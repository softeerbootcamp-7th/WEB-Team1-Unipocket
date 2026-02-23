import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '30s';
const THINK_TIME_SECONDS = toNumber(__ENV.THINK_TIME_SECONDS, 0.2);

const CLEANUP = toBoolean(__ENV.CLEANUP, false);
const DEBUG_FAILURES = toBoolean(__ENV.DEBUG_FAILURES, true);

const SEED_EXPENSE_COUNT = toNumber(__ENV.SEED_EXPENSE_COUNT, 30);
const COUNTRY_SWITCH_COUNT = toNumber(__ENV.COUNTRY_SWITCH_COUNT, 8);
const ANALYSIS_QUERIES_PER_SWITCH = toNumber(__ENV.ANALYSIS_QUERIES_PER_SWITCH, 2);
const BATCH_SETTLE_SECONDS = toNumber(__ENV.BATCH_SETTLE_SECONDS, 0);
const COUNTRY_SEQUENCE = parseCountrySequence(__ENV.COUNTRY_SEQUENCE || 'JP,US,GB,CA,DE');
const MAX_MERCHANT_NAME_LENGTH = 40;

const workflowSuccess = new Rate('analysis_workflow_success');
const countryUpdateSuccess = new Rate('analysis_country_update_success');
const analysisQuerySuccess = new Rate('analysis_query_success');
const seededExpenseSuccess = new Rate('analysis_seeded_expense_success');

const countryUpdateDuration = new Trend('analysis_country_update_duration', true);
const analysisQueryDuration = new Trend('analysis_query_duration', true);

export const options = {
	scenarios: {
		analysis_country_dirty: {
			executor: 'ramping-vus',
			startVUs: toNumber(__ENV.START_VUS, 1),
			gracefulRampDown: __ENV.GRACEFUL_RAMP_DOWN || '20s',
			stages: [
				{ duration: __ENV.STAGE_1_DURATION || '1m', target: toNumber(__ENV.STAGE_1_TARGET, 3) },
				{ duration: __ENV.STAGE_2_DURATION || '2m', target: toNumber(__ENV.STAGE_2_TARGET, 10) },
				{ duration: __ENV.STAGE_3_DURATION || '1m', target: toNumber(__ENV.STAGE_3_TARGET, 0) },
			],
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.1'],
		checks: ['rate>0.9'],
		analysis_workflow_success: ['rate>0.9'],
		analysis_country_update_success: ['rate>0.95'],
		analysis_query_success: ['rate>0.95'],
		analysis_seeded_expense_success: ['rate>0.95'],
	},
};

export default function analysisCountryDirtyWorkflow() {
	const now = new Date();
	const year = now.getUTCFullYear();
	const month = String(now.getUTCMonth() + 1).padStart(2, '0');
	const uniqueId = buildUniqueId();

	const ctx = {
		uniqueId,
		email: `k6-analysis-${uniqueId}@unipocket.local`,
		name: `k6-analysis-${__VU}`,
		accountBookTitle: `k6-analysis-book-${uniqueId}`,
		initialCountryCode: COUNTRY_SEQUENCE[0],
		analysisYear: String(year),
		analysisMonth: month,
		expenseOccurredAt: now.toISOString(),
	};

	let ok = false;
	try {
		if (!runDevAuth(ctx)) return;
		if (!runAccountBookSetup(ctx)) return;
		if (!seedExpenses(ctx)) return;
		if (!runCountrySwitchAndAnalysis(ctx)) return;
		if (!runPostBatchProbe(ctx)) return;
		ok = true;
	} finally {
		if (CLEANUP) {
			runCleanup(ctx);
		}
		workflowSuccess.add(ok);
		sleep(THINK_TIME_SECONDS);
	}
}

function runDevAuth(ctx) {
	return group('01.DevAuth', function () {
		const signUpRes = http.post(
			`${BASE_URL}/dev/sign-up`,
			JSON.stringify({
				email: ctx.email,
				name: ctx.name,
			}),
			params('dev-sign-up', null, true),
		);
		if (!statusIs(signUpRes, 200, 'dev-sign-up')) return false;

		const userId = parseJson(signUpRes, 'dev-sign-up');
		const userIdOk = check(userId, {
			'dev-sign-up returns uuid': (id) =>
				typeof id === 'string' &&
				/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(id),
		});
		if (!userIdOk) return false;
		ctx.userId = userId;

		const tokenRes = http.post(
			`${BASE_URL}/dev/token?userId=${encodeURIComponent(ctx.userId)}`,
			null,
			params('dev-token'),
		);
		if (!statusIs(tokenRes, 200, 'dev-token')) return false;

		const tokenBody = parseJson(tokenRes, 'dev-token');
		if (!tokenBody || typeof tokenBody.accessToken !== 'string') {
			return false;
		}
		ctx.accessToken = tokenBody.accessToken;
		return true;
	});
}

function runAccountBookSetup(ctx) {
	return group('02.AccountBookSetup', function () {
		const createRes = http.post(
			`${BASE_URL}/account-books`,
			JSON.stringify({
				localCountryCode: ctx.initialCountryCode,
				startDate: `${ctx.analysisYear}-01-01`,
				endDate: `${Number(ctx.analysisYear) + 1}-12-31`,
			}),
			authParams('account-books-create', ctx.accessToken, true),
		);
		if (!statusIs(createRes, 201, 'account-books-create')) return false;

		const body = parseJson(createRes, 'account-books-create');
		if (!body || !Number.isFinite(body.accountBookId)) return false;
		ctx.accountBookId = body.accountBookId;
		return true;
	});
}

function seedExpenses(ctx) {
	return group('03.SeedExpenses', function () {
		for (let i = 0; i < SEED_EXPENSE_COUNT; i += 1) {
			const requestBody = {
				merchantName: boundedMerchantName(`k6-analysis-merchant-${ctx.uniqueId}-${i}`),
				category: 2,
				userCardId: null,
				occurredAt: ctx.expenseOccurredAt,
				localCurrencyAmount: 1000 + i,
				localCurrencyCode: null,
				baseCurrencyAmount: null,
				memo: `analysis-seed-${i}`,
				travelId: null,
			};

			const createRes = http.post(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/manual`,
				JSON.stringify(requestBody),
				authParams('expenses-create-seed', ctx.accessToken, true),
			);
			const success = isStatus(createRes, 201);
			seededExpenseSuccess.add(success);
			if (!success) {
				logFailure('expenses-create-seed', createRes, requestBody);
				return false;
			}
		}

		return true;
	});
}

function runCountrySwitchAndAnalysis(ctx) {
	return group('04.CountrySwitchAndAnalysis', function () {
		let currentCountryIndex = 0;
		for (let i = 0; i < COUNTRY_SWITCH_COUNT; i += 1) {
			const nextCountryIndex = (currentCountryIndex + 1) % COUNTRY_SEQUENCE.length;
			const nextCountryCode = COUNTRY_SEQUENCE[nextCountryIndex];

			const patchBody = {
				localCountryCode: nextCountryCode,
			};
			const patchRes = http.patch(
				`${BASE_URL}/account-books/${ctx.accountBookId}`,
				JSON.stringify(patchBody),
				authParams('account-books-country-patch', ctx.accessToken, true),
			);
			countryUpdateDuration.add(patchRes.timings.duration);
			const patchSuccess = isStatus(patchRes, 200);
			countryUpdateSuccess.add(patchSuccess);
			if (!patchSuccess) {
				logFailure('account-books-country-patch', patchRes, patchBody);
				return false;
			}

			currentCountryIndex = nextCountryIndex;

			for (let q = 0; q < ANALYSIS_QUERIES_PER_SWITCH; q += 1) {
				const analysisRes = http.get(
					`${BASE_URL}/account-books/${ctx.accountBookId}/analysis?year=${ctx.analysisYear}&month=${ctx.analysisMonth}&currencyType=BASE`,
					authParams('analysis-overview-get', ctx.accessToken),
				);
				analysisQueryDuration.add(analysisRes.timings.duration);
				const success = isStatus(analysisRes, 200);
				analysisQuerySuccess.add(success);
				if (!success) {
					logFailure('analysis-overview-get', analysisRes);
					return false;
				}
			}
		}

		return true;
	});
}

function runPostBatchProbe(ctx) {
	if (BATCH_SETTLE_SECONDS <= 0) return true;

	return group('05.PostBatchProbe', function () {
		sleep(BATCH_SETTLE_SECONDS);
		const analysisRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/analysis?year=${ctx.analysisYear}&month=${ctx.analysisMonth}&currencyType=BASE`,
			authParams('analysis-overview-post-batch', ctx.accessToken),
		);
		analysisQueryDuration.add(analysisRes.timings.duration);
		const success = isStatus(analysisRes, 200);
		analysisQuerySuccess.add(success);
		if (!success) {
			logFailure('analysis-overview-post-batch', analysisRes);
		}
		return success;
	});
}

function runCleanup(ctx) {
	group('99.Cleanup', function () {
		if (Number.isFinite(ctx.accountBookId)) {
			const deleteRes = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}`,
				null,
				authParams('cleanup-account-book', ctx.accessToken),
			);
			statusIn(deleteRes, [204, 404], 'cleanup-account-book');
		}
	});
}

function parseJson(res, stepName) {
	try {
		return res.json();
	} catch (e) {
		check(null, {
			[`${stepName} json-parse`]: () => false,
		});
		return null;
	}
}

function statusIs(res, expectedStatus, stepName) {
	return check(res, {
		[`${stepName} status ${expectedStatus}`]: (r) => r && r.status === expectedStatus,
	});
}

function statusIn(res, expectedStatuses, stepName) {
	return check(res, {
		[`${stepName} status in [${expectedStatuses.join(',')}]`]: (r) =>
			r && expectedStatuses.includes(r.status),
	});
}

function isStatus(res, expectedStatus) {
	return !!res && res.status === expectedStatus;
}

function params(endpointName, accessToken = null, isJson = false) {
	const headers = {};
	if (isJson) {
		headers['Content-Type'] = 'application/json';
	}
	if (accessToken) {
		headers.Cookie = `access_token=${accessToken}`;
	}
	return {
		headers,
		tags: { endpoint: endpointName, name: endpointName },
		timeout: REQUEST_TIMEOUT,
	};
}

function authParams(endpointName, accessToken, isJson = false) {
	return params(endpointName, accessToken, isJson);
}

function buildUniqueId() {
	const randomPart = Math.floor(Math.random() * 46656)
		.toString(36)
		.padStart(3, '0');
	return `${Date.now().toString(36)}${__VU.toString(36)}${__ITER.toString(36)}${randomPart}`;
}

function boundedMerchantName(raw) {
	return String(raw).slice(0, MAX_MERCHANT_NAME_LENGTH);
}

function toNumber(value, fallback) {
	const parsed = Number(value);
	return Number.isFinite(parsed) ? parsed : fallback;
}

function toBoolean(value, fallback) {
	if (value === undefined || value === null) return fallback;
	const normalized = String(value).trim().toLowerCase();
	if (normalized === 'true') return true;
	if (normalized === 'false') return false;
	return fallback;
}

function parseCountrySequence(raw) {
	const parsed = String(raw)
		.split(',')
		.map((code) => code.trim().toUpperCase())
		.filter((code) => code.length > 0);
	if (parsed.length < 2) {
		return ['JP', 'US'];
	}
	return parsed;
}

function logFailure(stepName, res, requestPayload) {
	if (!DEBUG_FAILURES) return;
	const status = res ? res.status : 'NO_RESPONSE';
	const responseBody = res && res.body ? String(res.body).slice(0, 500) : '';
	const payload = requestPayload ? JSON.stringify(requestPayload) : '';
	console.log(
		`[k6-failure] step=${stepName} status=${status} request=${payload} response=${responseBody}`,
	);
}
