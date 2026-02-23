import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '30s';
const THINK_TIME_SECONDS = toNumber(__ENV.THINK_TIME_SECONDS, 0.2);

const CLEANUP = toBoolean(__ENV.CLEANUP, true);
const DEBUG_FAILURES = toBoolean(__ENV.DEBUG_FAILURES, true);
const QUERY_ROUNDS = toNumber(__ENV.QUERY_ROUNDS, 15);
const SEED_EXPENSE_COUNT = toNumber(__ENV.SEED_EXPENSE_COUNT, 20);
const QUERY_SLEEP_SECONDS = toNumber(__ENV.QUERY_SLEEP_SECONDS, 0);
const USE_BATCH_QUERIES = toBoolean(__ENV.USE_BATCH_QUERIES, true);
const MAX_MERCHANT_NAME_LENGTH = 40;

const workflowSuccess = new Rate('widget_workflow_success');
const widgetQuerySuccess = new Rate('widget_query_success');
const widgetRoundDuration = new Trend('widget_round_duration', true);

const WIDGET_TYPES = ['BUDGET', 'PERIOD', 'CATEGORY', 'COMPARISON', 'PAYMENT', 'CURRENCY'];

export const options = {
	scenarios: {
		widget_query_hotpath: {
			executor: 'ramping-vus',
			startVUs: toNumber(__ENV.START_VUS, 1),
			gracefulRampDown: __ENV.GRACEFUL_RAMP_DOWN || '20s',
			stages: [
				{ duration: __ENV.STAGE_1_DURATION || '1m', target: toNumber(__ENV.STAGE_1_TARGET, 5) },
				{ duration: __ENV.STAGE_2_DURATION || '3m', target: toNumber(__ENV.STAGE_2_TARGET, 20) },
				{ duration: __ENV.STAGE_3_DURATION || '1m', target: toNumber(__ENV.STAGE_3_TARGET, 0) },
			],
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.1'],
		checks: ['rate>0.9'],
		widget_workflow_success: ['rate>0.9'],
		widget_query_success: ['rate>0.95'],
	},
};

export default function widgetQueryWorkflow() {
	const uniqueId = buildUniqueId();
	const now = new Date();
	const year = now.getUTCFullYear();
	const ctx = {
		uniqueId,
		email: `k6-widget-${uniqueId}@unipocket.local`,
		name: `k6-widget-${__VU}`,
		accountBookId: null,
		travelId: null,
		expenseOccurredAt: now.toISOString(),
		accountBookTitle: `k6-widget-book-${uniqueId}`,
		travelPlaceName: `k6-widget-travel-${uniqueId}`,
		year: String(year),
	};

	let ok = false;
	try {
		if (!runDevAuth(ctx)) return;
		if (!createAccountBook(ctx)) return;
		if (!createTravel(ctx)) return;
		if (!seedExpenses(ctx)) return;
		if (!configureWidgetLayouts(ctx)) return;
		if (!runWidgetHotQueries(ctx)) return;
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
		if (!tokenBody || typeof tokenBody.accessToken !== 'string') return false;
		ctx.accessToken = tokenBody.accessToken;
		return true;
	});
}

function createAccountBook(ctx) {
	return group('02.AccountBookSetup', function () {
		const createRes = http.post(
			`${BASE_URL}/account-books`,
			JSON.stringify({
				localCountryCode: 'JP',
				startDate: `${ctx.year}-01-01`,
				endDate: `${Number(ctx.year) + 1}-12-31`,
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

function createTravel(ctx) {
	return group('03.TravelSetup', function () {
		const createBody = {
			travelPlaceName: ctx.travelPlaceName,
			startDate: `${ctx.year}-02-01`,
			endDate: `${ctx.year}-02-10`,
			imageKey: null,
		};
		const createRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels`,
			JSON.stringify(createBody),
			authParams('travels-create', ctx.accessToken, true),
		);
		if (!statusIs(createRes, 201, 'travels-create')) {
			logFailure('travels-create', createRes, createBody);
			return false;
		}

		ctx.travelId = idFromLocationHeader(createRes);
		return check(ctx.travelId, {
			'travel id resolved': (id) => Number.isFinite(id) && id > 0,
		});
	});
}

function seedExpenses(ctx) {
	return group('04.SeedExpenses', function () {
		for (let i = 0; i < SEED_EXPENSE_COUNT; i += 1) {
			const requestBody = {
				merchantName: boundedMerchantName(`k6-widget-merchant-${ctx.uniqueId}-${i}`),
				category: (i % 7) + 1,
				userCardId: null,
				occurredAt: ctx.expenseOccurredAt,
				localCurrencyAmount: 5000 + i * 10,
				localCurrencyCode: null,
				baseCurrencyAmount: null,
				memo: `widget-seed-${i}`,
				travelId: i % 2 === 0 ? ctx.travelId : null,
			};

			const createRes = http.post(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/manual`,
				JSON.stringify(requestBody),
				authParams('expenses-create-seed', ctx.accessToken, true),
			);
			if (!isStatus(createRes, 201)) {
				logFailure('expenses-create-seed', createRes, requestBody);
				return false;
			}
		}
		return true;
	});
}

function configureWidgetLayouts(ctx) {
	return group('05.WidgetLayoutSetup', function () {
		const accountBookWidgetLayoutRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/widgets`,
			JSON.stringify([
				{ order: 1, widgetType: 'BUDGET', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 2, widgetType: 'CATEGORY', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 3, widgetType: 'PAYMENT', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 4, widgetType: 'COMPARISON', currencyType: 'BASE', period: 'MONTHLY' },
			]),
			authParams('account-books-widgets-put', ctx.accessToken, true),
		);
		if (!statusIs(accountBookWidgetLayoutRes, 200, 'account-books-widgets-put')) return false;

		const travelWidgetLayoutRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widgets`,
			JSON.stringify([
				{ order: 1, widgetType: 'PERIOD', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 2, widgetType: 'CATEGORY', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 3, widgetType: 'PAYMENT', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 4, widgetType: 'CURRENCY', currencyType: 'BASE', period: 'MONTHLY' },
			]),
			authParams('travel-widgets-put', ctx.accessToken, true),
		);
		return statusIs(travelWidgetLayoutRes, 200, 'travel-widgets-put');
	});
}

function runWidgetHotQueries(ctx) {
	return group('06.WidgetQueryHotLoop', function () {
		for (let round = 0; round < QUERY_ROUNDS; round += 1) {
			const startedAt = Date.now();
			const roundSuccess = USE_BATCH_QUERIES
				? runWidgetQueryRoundBatch(ctx, round)
				: runWidgetQueryRoundSequential(ctx, round);
			widgetRoundDuration.add(Date.now() - startedAt);
			widgetQuerySuccess.add(roundSuccess);
			if (!roundSuccess) {
				return false;
			}
			if (QUERY_SLEEP_SECONDS > 0) {
				sleep(QUERY_SLEEP_SECONDS);
			}
		}
		return true;
	});
}

function runWidgetQueryRoundBatch(ctx, round) {
	const requests = [];

	requests.push([
		'GET',
		`${BASE_URL}/account-books/${ctx.accountBookId}/widgets`,
		null,
		authParams('account-widget-layout', ctx.accessToken),
	]);
	requests.push([
		'GET',
		`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widgets`,
		null,
		authParams('travel-widget-layout', ctx.accessToken),
	]);

	for (let i = 0; i < WIDGET_TYPES.length; i += 1) {
		const widgetType = WIDGET_TYPES[i];
		requests.push([
			'GET',
			`${BASE_URL}/account-books/${ctx.accountBookId}/widget?widgetType=${widgetType}&currencyType=BASE&period=MONTHLY`,
			null,
			authParams(`account-widget-${widgetType}`, ctx.accessToken),
		]);
		requests.push([
			'GET',
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widget?widgetType=${widgetType}&currencyType=BASE&period=MONTHLY`,
			null,
			authParams(`travel-widget-${widgetType}`, ctx.accessToken),
		]);
	}

	const responses = http.batch(requests);
	for (let i = 0; i < responses.length; i += 1) {
		const res = responses[i];
		if (!res || res.status !== 200) {
			logFailure(`widget-query-round-batch-${i}`, res);
			return false;
		}
	}
	return true;
}

function runWidgetQueryRoundSequential(ctx, round) {
	const layoutAccountRes = http.get(
		`${BASE_URL}/account-books/${ctx.accountBookId}/widgets`,
		authParams('account-widget-layout-seq', ctx.accessToken),
	);
	if (!isStatus(layoutAccountRes, 200)) return false;

	const layoutTravelRes = http.get(
		`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widgets`,
		authParams('travel-widget-layout-seq', ctx.accessToken),
	);
	if (!isStatus(layoutTravelRes, 200)) return false;

	for (let i = 0; i < WIDGET_TYPES.length; i += 1) {
		const widgetType = WIDGET_TYPES[i];
		const accountRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/widget?widgetType=${widgetType}&currencyType=BASE&period=MONTHLY`,
			authParams(`account-widget-seq-${widgetType}`, ctx.accessToken),
		);
		if (!isStatus(accountRes, 200)) return false;

		const travelRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widget?widgetType=${widgetType}&currencyType=BASE&period=MONTHLY`,
			authParams(`travel-widget-seq-${widgetType}`, ctx.accessToken),
		);
		if (!isStatus(travelRes, 200)) return false;
	}
	return true;
}

function runCleanup(ctx) {
	group('99.Cleanup', function () {
		if (Number.isFinite(ctx.travelId) && Number.isFinite(ctx.accountBookId)) {
			const travelDeleteRes = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}`,
				null,
				authParams('cleanup-travel', ctx.accessToken),
			);
			statusIn(travelDeleteRes, [204, 404], 'cleanup-travel');
		}

		if (Number.isFinite(ctx.accountBookId)) {
			const accountDeleteRes = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}`,
				null,
				authParams('cleanup-account-book', ctx.accessToken),
			);
			statusIn(accountDeleteRes, [204, 404], 'cleanup-account-book');
		}
	});
}

function idFromLocationHeader(res) {
	const location = getHeader(res, 'Location');
	if (!location) return null;
	const match = String(location).match(/\/(\d+)(?:\?.*)?$/);
	return match ? Number(match[1]) : null;
}

function getHeader(res, headerName) {
	if (!res || !res.headers) return null;
	const direct = res.headers[headerName];
	if (direct !== undefined) {
		return Array.isArray(direct) ? direct[0] : direct;
	}
	const lowerKey = headerName.toLowerCase();
	const foundKey = Object.keys(res.headers).find((k) => k.toLowerCase() === lowerKey);
	if (!foundKey) return null;
	const value = res.headers[foundKey];
	return Array.isArray(value) ? value[0] : value;
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

function logFailure(stepName, res, requestPayload) {
	if (!DEBUG_FAILURES) return;
	const status = res ? res.status : 'NO_RESPONSE';
	const responseBody = res && res.body ? String(res.body).slice(0, 500) : '';
	const payload = requestPayload ? JSON.stringify(requestPayload) : '';
	console.log(
		`[k6-failure] step=${stepName} status=${status} request=${payload} response=${responseBody}`,
	);
}
