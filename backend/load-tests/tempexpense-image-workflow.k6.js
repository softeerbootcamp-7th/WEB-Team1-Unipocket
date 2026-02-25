import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '300s';
const THINK_TIME_SECONDS = toNumber(__ENV.THINK_TIME_SECONDS, 0.2);

const CLEANUP = toBoolean(__ENV.CLEANUP, true);
const DEBUG_FAILURES = toBoolean(__ENV.DEBUG_FAILURES, true);

const IMAGE_COUNT = clamp(toNumber(__ENV.IMAGE_COUNT, 2), 1, 3);
const POLL_INTERVAL_SECONDS = toNumber(__ENV.POLL_INTERVAL_SECONDS, 1);
const PARSE_TIMEOUT_SECONDS = toNumber(__ENV.PARSE_TIMEOUT_SECONDS, 60);
const CONFIRM_TIMEOUT_SECONDS = toNumber(__ENV.CONFIRM_TIMEOUT_SECONDS, 60);
const PRESIGNED_URL_HOST_OVERRIDE = (__ENV.PRESIGNED_URL_HOST_OVERRIDE || '').trim();

const workflowSuccess = new Rate('tempexpense_workflow_success');
const uploadSuccess = new Rate('tempexpense_upload_success');
const parseStartSuccess = new Rate('tempexpense_parse_start_success');
const parseCompletionSuccess = new Rate('tempexpense_parse_completion_success');
const confirmStartSuccess = new Rate('tempexpense_confirm_start_success');
const confirmCompletionSuccess = new Rate('tempexpense_confirm_completion_success');

const parseCompletionDuration = new Trend('tempexpense_parse_completion_duration', true);
const confirmCompletionDuration = new Trend('tempexpense_confirm_completion_duration', true);

const DUMMY_IMAGE_BODY = 'k6-dummy-image-content';

export const options = {
	scenarios: {
		tempexpense_image_workflow: {
			executor: 'ramping-vus',
			startVUs: toNumber(__ENV.START_VUS, 1),
			gracefulRampDown: __ENV.GRACEFUL_RAMP_DOWN || '20s',
			stages: [
				{ duration: __ENV.STAGE_1_DURATION || '1m', target: toNumber(__ENV.STAGE_1_TARGET, 2) },
				{ duration: __ENV.STAGE_2_DURATION || '2m', target: toNumber(__ENV.STAGE_2_TARGET, 8) },
				{ duration: __ENV.STAGE_3_DURATION || '1m', target: toNumber(__ENV.STAGE_3_TARGET, 0) },
			],
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.1'],
		checks: ['rate>0.9'],
		tempexpense_workflow_success: ['rate>0.9'],
		tempexpense_upload_success: ['rate>0.95'],
		tempexpense_parse_start_success: ['rate>0.95'],
		tempexpense_confirm_start_success: ['rate>0.95'],
	},
};

export default function tempExpenseImageWorkflow() {
	const uniqueId = buildUniqueId();
	const now = new Date();
	const year = now.getUTCFullYear();

	const ctx = {
		uniqueId,
		email: `k6-temp-${uniqueId}@unipocket.local`,
		name: `k6-temp-${__VU}`,
		accountBookId: null,
		tempExpenseMetaId: null,
		s3Keys: [],
		imageCount: IMAGE_COUNT,
		expenseSeedYear: String(year),
	};

	let ok = false;
	try {
		if (!runDevAuth(ctx)) return;
		if (!createAccountBook(ctx)) return;
		if (!uploadImages(ctx)) return;
		if (!startAndWaitParse(ctx)) return;
		if (!startAndWaitConfirm(ctx)) return;
		if (!verifyConvertedExpenses(ctx)) return;
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
				startDate: `${ctx.expenseSeedYear}-01-01`,
				endDate: `${Number(ctx.expenseSeedYear) + 1}-12-31`,
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

function uploadImages(ctx) {
	return group('03.UploadImages', function () {
		for (let i = 0; i < ctx.imageCount; i += 1) {
			const issueBody = {
				fileName: `k6-temp-${ctx.uniqueId}-${i}.png`,
				mimeType: 'image/png',
				uploadType: 'IMAGE',
				tempExpenseMetaId: ctx.tempExpenseMetaId,
			};
			const issueRes = http.post(
				`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expenses/uploads/presigned-url`,
				JSON.stringify(issueBody),
				authParams('temp-presigned-url', ctx.accessToken, true),
			);
			if (!statusIs(issueRes, 200, 'temp-presigned-url')) {
				logFailure('temp-presigned-url', issueRes, issueBody);
				return false;
			}

			const issueParsed = parseJson(issueRes, 'temp-presigned-url');
			if (
				!issueParsed ||
				!Number.isFinite(issueParsed.tempExpenseMetaId) ||
				typeof issueParsed.presignedUrl !== 'string' ||
				typeof issueParsed.s3Key !== 'string'
			) {
				return false;
			}

			ctx.tempExpenseMetaId = issueParsed.tempExpenseMetaId;
			ctx.s3Keys.push(issueParsed.s3Key);

			const uploadUrl = normalizePresignedUrl(issueParsed.presignedUrl);
			const uploadRes = http.put(uploadUrl, DUMMY_IMAGE_BODY, {
				headers: {
					'Content-Type': 'image/png',
				},
				tags: { endpoint: 'temp-presigned-upload-put', name: 'temp-presigned-upload-put' },
				timeout: REQUEST_TIMEOUT,
			});

			const success = uploadRes && (uploadRes.status === 200 || uploadRes.status === 204);
			uploadSuccess.add(success);
			if (!success) {
				logFailure('temp-presigned-upload-put', uploadRes, { uploadUrl });
				return false;
			}
		}
		return true;
	});
}

function startAndWaitParse(ctx) {
	return group('04.ParseWorkflow', function () {
		const parseBody = {
			tempExpenseMetaId: ctx.tempExpenseMetaId,
			s3Keys: ctx.s3Keys,
		};
		const parseRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expenses/parse`,
			JSON.stringify(parseBody),
			authParams('temp-parse-start', ctx.accessToken, true),
		);
		const parseStarted = isStatus(parseRes, 202);
		parseStartSuccess.add(parseStarted);
		if (!parseStarted) {
			logFailure('temp-parse-start', parseRes, parseBody);
			return false;
		}

		const startedAt = Date.now();
		const parseCompleted = waitForCondition(PARSE_TIMEOUT_SECONDS, function () {
			const filesRes = http.get(
				`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}/files`,
				authParams('temp-files-after-parse', ctx.accessToken),
			);
			if (!isStatus(filesRes, 200)) {
				logFailure('temp-files-after-parse', filesRes);
				return false;
			}
			const body = parseJson(filesRes, 'temp-files-after-parse');
			if (!body) return false;
			const totalTempExpenses = countTemporaryExpenses(body);
			return totalTempExpenses > 0;
		});
		parseCompletionDuration.add(Date.now() - startedAt);
		parseCompletionSuccess.add(parseCompleted);
		if (!parseCompleted) {
			return false;
		}
		return true;
	});
}

function startAndWaitConfirm(ctx) {
	return group('05.ConfirmWorkflow', function () {
		const confirmRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}/confirm`,
			null,
			authParams('temp-confirm-start', ctx.accessToken),
		);
		const confirmStarted = isStatus(confirmRes, 202);
		confirmStartSuccess.add(confirmStarted);
		if (!confirmStarted) {
			logFailure('temp-confirm-start', confirmRes);
			return false;
		}

		const startedAt = Date.now();
		const confirmCompleted = waitForCondition(CONFIRM_TIMEOUT_SECONDS, function () {
			const filesRes = http.get(
				`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}/files`,
				authParams('temp-files-after-confirm', ctx.accessToken),
			);
			if (!isStatus(filesRes, 200)) {
				logFailure('temp-files-after-confirm', filesRes);
				return false;
			}
			const body = parseJson(filesRes, 'temp-files-after-confirm');
			if (!body) return false;
			return countTemporaryExpenses(body) === 0;
		});
		confirmCompletionDuration.add(Date.now() - startedAt);
		confirmCompletionSuccess.add(confirmCompleted);
		return confirmCompleted;
	});
}

function verifyConvertedExpenses(ctx) {
	return group('06.VerifyConvertedExpense', function () {
		const listRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses?page=0&size=20&sort=occurredAt,desc`,
			authParams('expenses-list-after-confirm', ctx.accessToken),
		);
		if (!statusIs(listRes, 200, 'expenses-list-after-confirm')) return false;

		const body = parseJson(listRes, 'expenses-list-after-confirm');
		if (!body || !Array.isArray(body.expenses)) return false;

		const hasConvertedExpense = body.expenses.some((expense) => expense.source === 'IMAGE_RECEIPT');
		return check(hasConvertedExpense, {
			'converted expense from IMAGE_RECEIPT exists': (v) => v === true,
		});
	});
}

function runCleanup(ctx) {
	group('99.Cleanup', function () {
		if (Number.isFinite(ctx.tempExpenseMetaId) && Number.isFinite(ctx.accountBookId)) {
			const metaDeleteRes = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}`,
				null,
				authParams('cleanup-temp-meta', ctx.accessToken),
			);
			statusIn(metaDeleteRes, [204, 404], 'cleanup-temp-meta');
		}

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

function waitForCondition(timeoutSeconds, conditionFn) {
	const deadline = Date.now() + timeoutSeconds * 1000;
	while (Date.now() <= deadline) {
		if (conditionFn()) {
			return true;
		}
		sleep(POLL_INTERVAL_SECONDS);
	}
	return false;
}

function countTemporaryExpenses(filesResponse) {
	if (!filesResponse || !Array.isArray(filesResponse.files)) return 0;
	let total = 0;
	for (let i = 0; i < filesResponse.files.length; i += 1) {
		const file = filesResponse.files[i];
		if (file && Array.isArray(file.expenses)) {
			total += file.expenses.length;
		}
	}
	return total;
}

function normalizePresignedUrl(url) {
	if (!PRESIGNED_URL_HOST_OVERRIDE) {
		return url;
	}
	const override = PRESIGNED_URL_HOST_OVERRIDE.replace(/\/+$/, '');
	return String(url).replace(/^https?:\/\/[^/]+/, override);
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
	const randomPart = Math.floor(Math.random() * 1000000)
		.toString()
		.padStart(6, '0');
	return `${Date.now()}-${__VU}-${__ITER}-${randomPart}`;
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

function clamp(value, min, max) {
	return Math.max(min, Math.min(max, value));
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
