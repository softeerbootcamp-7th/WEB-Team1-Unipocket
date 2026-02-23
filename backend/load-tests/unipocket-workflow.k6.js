import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '30s';
const THINK_TIME_SECONDS = toNumber(__ENV.THINK_TIME_SECONDS, 0.2);

const CLEANUP = toBoolean(__ENV.CLEANUP, true);
const ENABLE_BUDGET_UPDATE = toBoolean(__ENV.ENABLE_BUDGET_UPDATE, false);
const ENABLE_EXCHANGE_RATE_QUERY = toBoolean(__ENV.ENABLE_EXCHANGE_RATE_QUERY, false);
const ENABLE_TEMP_EXPENSE = toBoolean(__ENV.ENABLE_TEMP_EXPENSE, false);
const ENABLE_USER_WITHDRAW = toBoolean(__ENV.ENABLE_USER_WITHDRAW, false);
const DEBUG_FAILURES = toBoolean(__ENV.DEBUG_FAILURES, true);
const MAX_MERCHANT_NAME_LENGTH = 40;

const workflowSuccess = new Rate('workflow_success');

export const options = {
	scenarios: {
		workflow: {
			executor: 'ramping-vus',
			startVUs: toNumber(__ENV.START_VUS, 1),
			gracefulRampDown: __ENV.GRACEFUL_RAMP_DOWN || '10s',
			stages: [
				{
					duration: __ENV.STAGE_1_DURATION || '30s',
					target: toNumber(__ENV.STAGE_1_TARGET, 5),
				},
				{
					duration: __ENV.STAGE_2_DURATION || '1m',
					target: toNumber(__ENV.STAGE_2_TARGET, 15),
				},
				{
					duration: __ENV.STAGE_3_DURATION || '30s',
					target: toNumber(__ENV.STAGE_3_TARGET, 0),
				},
			],
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.05'],
		checks: ['rate>0.95'],
		workflow_success: ['rate>0.95'],
	},
};

export default function workflow() {
	const uniqueId = buildUniqueId();
	const ctx = {
		uniqueId,
		email: `k6-${uniqueId}@unipocket.local`,
		name: `k6-user-${__VU}`,
		cardNickname: `k6-card-${uniqueId}`,
		accountBookTitle: `k6-book-${uniqueId}`,
		travelPlaceName: `k6-trip-${uniqueId}`,
		merchantA: boundedMerchantName(`k6-merchant-a-${uniqueId}`),
		merchantB: boundedMerchantName(`k6-merchant-b-${uniqueId}`),
		cardNumber: String((1000 + (__VU + __ITER) % 9000)).padStart(4, '0'),
		occurredAtIso: new Date().toISOString(),
	};

	let ok = false;
	try {
		if (!runHealthCheck()) return;
		if (!runDevAuth(ctx)) return;
		if (!runUserCardFlow(ctx)) return;
		if (!runAccountBookFlow(ctx)) return;
		if (!runTravelFlow(ctx)) return;
		if (!runExpenseFlow(ctx)) return;
		if (!runWidgetFlow(ctx)) return;
		if (!runAnalysisFlow(ctx)) return;
		if (!runTempExpenseFlow(ctx)) return;

		ok = true;
	} finally {
		if (CLEANUP) {
			runCleanupFlow(ctx);
		}
		workflowSuccess.add(ok);
		sleep(THINK_TIME_SECONDS);
	}
}

function runHealthCheck() {
	return group('00.HealthCheck', function () {
		const res = http.get(`${BASE_URL}/health-check`, params('health-check'));
		return statusIs(res, 200, 'health-check');
	});
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
		if (!tokenBody) return false;

		const tokenOk = check(tokenBody, {
			'dev-token returns accessToken': (b) => typeof b.accessToken === 'string' && b.accessToken.length > 20,
			'dev-token returns refreshToken': (b) =>
				typeof b.refreshToken === 'string' && b.refreshToken.length > 20,
		});
		if (!tokenOk) return false;

		ctx.accessToken = tokenBody.accessToken;
		ctx.refreshToken = tokenBody.refreshToken;
		return true;
	});
}

function runUserCardFlow(ctx) {
	return group('02.UserCardFlow', function () {
		const meRes = http.get(`${BASE_URL}/users/me`, authParams('users-me', ctx.accessToken));
		if (!statusIs(meRes, 200, 'users-me')) return false;

		const companiesRes = http.get(
			`${BASE_URL}/users/cards/companies`,
			params('users-cards-companies'),
		);
		if (!statusIs(companiesRes, 200, 'users-cards-companies')) return false;

		const createCardRes = http.post(
			`${BASE_URL}/users/cards`,
			JSON.stringify({
				nickName: ctx.cardNickname,
				cardNumber: ctx.cardNumber,
				cardCompany: 'HYUNDAI',
			}),
			authParams('users-cards-create', ctx.accessToken, true),
		);
		if (!statusIs(createCardRes, 201, 'users-cards-create')) return false;

		const createdCardId = idFromLocationHeader(createCardRes);
		const cardsRes = http.get(`${BASE_URL}/users/cards`, authParams('users-cards-list', ctx.accessToken));
		if (!statusIs(cardsRes, 200, 'users-cards-list')) return false;

		const cardsBody = parseJson(cardsRes, 'users-cards-list');
		const listedCardId = findCardId(cardsBody, ctx.cardNickname, ctx.cardNumber);
		ctx.cardId = createdCardId || listedCardId;

		return check(ctx.cardId, {
			'card id resolved': (id) => Number.isFinite(id) && id > 0,
		});
	});
}

function runAccountBookFlow(ctx) {
	return group('03.AccountBookFlow', function () {
		const createRes = http.post(
			`${BASE_URL}/account-books`,
			JSON.stringify({
				localCountryCode: 'JP',
				startDate: '2025-01-01',
				endDate: '2027-12-31',
			}),
			authParams('account-books-create', ctx.accessToken, true),
		);
		if (!statusIs(createRes, 201, 'account-books-create')) return false;

		const createBody = parseJson(createRes, 'account-books-create');
		if (!createBody || !Number.isFinite(createBody.accountBookId)) return false;
		ctx.accountBookId = createBody.accountBookId;

		const listRes = http.get(`${BASE_URL}/account-books`, authParams('account-books-list', ctx.accessToken));
		if (!statusIs(listRes, 200, 'account-books-list')) return false;

		const detailRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}`,
			authParams('account-books-detail', ctx.accessToken),
		);
		if (!statusIs(detailRes, 200, 'account-books-detail')) return false;

		const patchRes = http.patch(
			`${BASE_URL}/account-books/${ctx.accountBookId}`,
			JSON.stringify({
				title: ctx.accountBookTitle,
			}),
			authParams('account-books-patch', ctx.accessToken, true),
		);
		if (!statusIs(patchRes, 200, 'account-books-patch')) return false;

		if (ENABLE_EXCHANGE_RATE_QUERY) {
			const exchangeRes = http.get(
				`${BASE_URL}/account-books/${ctx.accountBookId}/exchange-rate?occurredAt=2026-02-01T12:00:00`,
				authParams('account-books-exchange-rate', ctx.accessToken),
			);
			if (!statusIs(exchangeRes, 200, 'account-books-exchange-rate')) return false;
		}

		if (ENABLE_BUDGET_UPDATE) {
			const budgetRes = http.patch(
				`${BASE_URL}/account-books/${ctx.accountBookId}/budget`,
				JSON.stringify({ budget: 1000000 }),
				authParams('account-books-budget', ctx.accessToken, true),
			);
			if (!statusIs(budgetRes, 200, 'account-books-budget')) return false;
		}

		return true;
	});
}

function runTravelFlow(ctx) {
	return group('04.TravelFlow', function () {
		const createRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels`,
			JSON.stringify({
				travelPlaceName: ctx.travelPlaceName,
				startDate: '2026-02-01',
				endDate: '2026-02-10',
				imageKey: null,
			}),
			authParams('travels-create', ctx.accessToken, true),
		);
		if (!statusIs(createRes, 201, 'travels-create')) return false;

		const createdTravelId = idFromLocationHeader(createRes);
		const listRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels`,
			authParams('travels-list', ctx.accessToken),
		);
		if (!statusIs(listRes, 200, 'travels-list')) return false;

		const listBody = parseJson(listRes, 'travels-list');
		const listedTravelId = findTravelId(listBody, ctx.travelPlaceName);
		ctx.travelId = createdTravelId || listedTravelId;

		const idOk = check(ctx.travelId, {
			'travel id resolved': (id) => Number.isFinite(id) && id > 0,
		});
		if (!idOk) return false;

		const detailRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}`,
			authParams('travels-detail', ctx.accessToken),
		);
		if (!statusIs(detailRes, 200, 'travels-detail')) return false;

		const patchRes = http.patch(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}`,
			JSON.stringify({
				travelPlaceName: `${ctx.travelPlaceName}-patched`,
				startDate: '2026-02-01',
				endDate: '2026-02-10',
				imageKey: null,
			}),
			authParams('travels-patch', ctx.accessToken, true),
		);

		return statusIs(patchRes, 200, 'travels-patch');
	});
}

function runExpenseFlow(ctx) {
	return group('05.ExpenseFlow', function () {
		const createARequest = {
			merchantName: ctx.merchantA,
			category: 2,
			userCardId: ctx.cardId,
			occurredAt: ctx.occurredAtIso,
			localCurrencyAmount: 12000.5,
			localCurrencyCode: 'KRW',
			baseCurrencyAmount: null,
			memo: 'k6 expense a',
			travelId: ctx.travelId,
		};
		let createARes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/manual`,
			JSON.stringify(createARequest),
			authParams('expenses-create-a', ctx.accessToken, true),
		);
		if (!isStatus(createARes, 201)) {
			logFailure('expenses-create-a', createARes, createARequest);

			const createAFallbackRequest = {
				...createARequest,
				userCardId: null,
				travelId: null,
				memo: 'k6 expense a fallback',
			};
			createARes = http.post(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/manual`,
				JSON.stringify(createAFallbackRequest),
				authParams('expenses-create-a-fallback', ctx.accessToken, true),
			);
			if (!isStatus(createARes, 201)) {
				logFailure(
					'expenses-create-a-fallback',
					createARes,
					createAFallbackRequest,
				);
				return false;
			}
		}
		if (!statusIs(createARes, 201, 'expenses-create-a')) return false;

		const createABody = parseJson(createARes, 'expenses-create-a');
		ctx.expenseIdA = createABody && createABody.expenseId;
		if (!Number.isFinite(ctx.expenseIdA)) return false;

		const createBRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/manual`,
			JSON.stringify({
				merchantName: ctx.merchantB,
				category: 3,
				userCardId: null,
				occurredAt: ctx.occurredAtIso,
				localCurrencyAmount: 8300,
				localCurrencyCode: 'KRW',
				baseCurrencyAmount: null,
				memo: 'k6 expense b',
				travelId: ctx.travelId,
			}),
			authParams('expenses-create-b', ctx.accessToken, true),
		);
		if (!statusIs(createBRes, 201, 'expenses-create-b')) return false;

		const createBBody = parseJson(createBRes, 'expenses-create-b');
		ctx.expenseIdB = createBBody && createBBody.expenseId;
		if (!Number.isFinite(ctx.expenseIdB)) return false;

		const listRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses?page=0&size=20&sort=occurredAt,desc`,
			authParams('expenses-list', ctx.accessToken),
		);
		if (!statusIs(listRes, 200, 'expenses-list')) return false;

		const detailRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/${ctx.expenseIdA}`,
			authParams('expenses-detail', ctx.accessToken),
		);
		if (!statusIs(detailRes, 200, 'expenses-detail')) return false;

		const updateRequest = {
			merchantName: boundedMerchantName(`${ctx.merchantA}-updated`),
			category: 2,
			userCardId: ctx.cardId,
			occurredAt: ctx.occurredAtIso,
			localCurrencyAmount: 15000.0,
			localCurrencyCode: 'KRW',
			baseCurrencyAmount: null,
			memo: 'k6 expense a updated',
			travelId: ctx.travelId,
		};
		let updateRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/${ctx.expenseIdA}`,
			JSON.stringify(updateRequest),
			authParams('expenses-update', ctx.accessToken, true),
		);
		if (!isStatus(updateRes, 200)) {
			logFailure('expenses-update', updateRes, updateRequest);

			const updateFallbackRequest = {
				...updateRequest,
				userCardId: null,
				travelId: null,
				memo: 'k6 expense a updated fallback',
			};
			updateRes = http.put(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/${ctx.expenseIdA}`,
				JSON.stringify(updateFallbackRequest),
				authParams('expenses-update-fallback', ctx.accessToken, true),
			);
			if (!isStatus(updateRes, 200)) {
				logFailure('expenses-update-fallback', updateRes, updateFallbackRequest);
				return false;
			}
		}
		if (!statusIs(updateRes, 200, 'expenses-update')) return false;

		const bulkRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/bulk`,
			JSON.stringify({
				items: [
					{
						expenseId: ctx.expenseIdA,
						merchantName: boundedMerchantName(`${ctx.merchantA}-bulk`),
						category: 2,
						userCardId: ctx.cardId,
						occurredAt: ctx.occurredAtIso,
						localCurrencyAmount: 11111.0,
						localCurrencyCode: 'KRW',
						baseCurrencyAmount: null,
						memo: 'bulk-a',
						travelId: ctx.travelId,
					},
					{
						expenseId: ctx.expenseIdB,
						merchantName: boundedMerchantName(`${ctx.merchantB}-bulk`),
						category: 3,
						userCardId: null,
						occurredAt: ctx.occurredAtIso,
						localCurrencyAmount: 22222.0,
						localCurrencyCode: 'KRW',
						baseCurrencyAmount: null,
						memo: 'bulk-b',
						travelId: ctx.travelId,
					},
				],
			}),
			authParams('expenses-bulk-update', ctx.accessToken, true),
		);
		if (!statusIs(bulkRes, 200, 'expenses-bulk-update')) return false;

		const merchantSearchRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/merchant-names?q=${encodeURIComponent(ctx.merchantA.slice(0, 10))}&limit=10`,
			authParams('expenses-merchant-search', ctx.accessToken),
		);
		if (!statusIs(merchantSearchRes, 200, 'expenses-merchant-search')) return false;

		const occurredAt = new Date(ctx.occurredAtIso);
		ctx.analysisYear = String(occurredAt.getUTCFullYear());
		ctx.analysisMonth = String(occurredAt.getUTCMonth() + 1).padStart(2, '0');
		return true;
	});
}

function runWidgetFlow(ctx) {
	return group('06.WidgetFlow', function () {
		const accountBookWidgetLayoutRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/widgets`,
			authParams('account-books-widgets-get', ctx.accessToken),
		);
		if (!statusIs(accountBookWidgetLayoutRes, 200, 'account-books-widgets-get')) return false;

		const updateAccountBookWidgetsRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/widgets`,
			JSON.stringify([
				{ order: 1, widgetType: 'BUDGET', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 2, widgetType: 'CATEGORY', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 3, widgetType: 'PAYMENT', currencyType: 'BASE', period: 'MONTHLY' },
			]),
			authParams('account-books-widgets-put', ctx.accessToken, true),
		);
		if (!statusIs(updateAccountBookWidgetsRes, 200, 'account-books-widgets-put')) return false;

		const accountBookWidgetDataRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/widget?widgetType=PAYMENT&currencyType=BASE&period=MONTHLY`,
			authParams('account-books-widget-get', ctx.accessToken),
		);
		if (!statusIs(accountBookWidgetDataRes, 200, 'account-books-widget-get')) return false;

		const updateTravelWidgetsRes = http.put(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widgets`,
			JSON.stringify([
				{ order: 1, widgetType: 'PERIOD', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 2, widgetType: 'CATEGORY', currencyType: 'BASE', period: 'MONTHLY' },
				{ order: 3, widgetType: 'PAYMENT', currencyType: 'BASE', period: 'MONTHLY' },
			]),
			authParams('travel-widgets-put', ctx.accessToken, true),
		);
		if (!statusIs(updateTravelWidgetsRes, 200, 'travel-widgets-put')) return false;

		const travelWidgetLayoutRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widgets`,
			authParams('travel-widgets-get', ctx.accessToken),
		);
		if (!statusIs(travelWidgetLayoutRes, 200, 'travel-widgets-get')) return false;

		const travelWidgetDataRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}/widget?widgetType=PAYMENT&currencyType=BASE&period=MONTHLY`,
			authParams('travel-widget-get', ctx.accessToken),
		);
		return statusIs(travelWidgetDataRes, 200, 'travel-widget-get');
	});
}

function runAnalysisFlow(ctx) {
	return group('07.AnalysisFlow', function () {
		const year = ctx.analysisYear || String(new Date().getUTCFullYear());
		const month = ctx.analysisMonth || String(new Date().getUTCMonth() + 1).padStart(2, '0');

		const analysisRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/analysis?year=${year}&month=${month}&currencyType=BASE`,
			authParams('analysis-get', ctx.accessToken),
		);
		return statusIs(analysisRes, 200, 'analysis-get');
	});
}

function runTempExpenseFlow(ctx) {
	if (!ENABLE_TEMP_EXPENSE) return true;

	return group('08.TempExpenseFlow(Optional)', function () {
		const issueUploadRes = http.post(
			`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expenses/uploads/presigned-url`,
			JSON.stringify({
				fileName: `k6-${ctx.uniqueId}.png`,
				mimeType: 'image/png',
				uploadType: 'IMAGE',
				tempExpenseMetaId: null,
			}),
			authParams('temp-expense-presigned-url', ctx.accessToken, true),
		);
		if (!statusIs(issueUploadRes, 200, 'temp-expense-presigned-url')) return false;

		const issueBody = parseJson(issueUploadRes, 'temp-expense-presigned-url');
		if (!issueBody || !Number.isFinite(issueBody.tempExpenseMetaId)) return false;
		ctx.tempExpenseMetaId = issueBody.tempExpenseMetaId;

		const metaListRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas`,
			authParams('temp-expense-meta-list', ctx.accessToken),
		);
		if (!statusIs(metaListRes, 200, 'temp-expense-meta-list')) return false;

		const fileListRes = http.get(
			`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}/files`,
			authParams('temp-expense-files-list', ctx.accessToken),
		);
		return statusIs(fileListRes, 200, 'temp-expense-files-list');
	});
}

function runCleanupFlow(ctx) {
	group('99.Cleanup', function () {
		if (Number.isFinite(ctx.expenseIdA) && Number.isFinite(ctx.accountBookId)) {
			const res = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/${ctx.expenseIdA}`,
				null,
				authParams('cleanup-expense-a', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-expense-a');
		}

		if (Number.isFinite(ctx.expenseIdB) && Number.isFinite(ctx.accountBookId)) {
			const res = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/expenses/${ctx.expenseIdB}`,
				null,
				authParams('cleanup-expense-b', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-expense-b');
		}

		if (Number.isFinite(ctx.travelId) && Number.isFinite(ctx.accountBookId)) {
			const res = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/travels/${ctx.travelId}`,
				null,
				authParams('cleanup-travel', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-travel');
		}

		if (Number.isFinite(ctx.cardId)) {
			const res = http.del(
				`${BASE_URL}/users/cards/${ctx.cardId}`,
				null,
				authParams('cleanup-card', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-card');
		}

		if (Number.isFinite(ctx.tempExpenseMetaId) && Number.isFinite(ctx.accountBookId)) {
			const res = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}/temporary-expense-metas/${ctx.tempExpenseMetaId}`,
				null,
				authParams('cleanup-temp-expense-meta', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-temp-expense-meta');
		}

		if (Number.isFinite(ctx.accountBookId)) {
			const res = http.del(
				`${BASE_URL}/account-books/${ctx.accountBookId}`,
				null,
				authParams('cleanup-account-book', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-account-book');
		}

		if (ENABLE_USER_WITHDRAW) {
			const res = http.del(
				`${BASE_URL}/users/me`,
				null,
				authParams('cleanup-user-withdraw', ctx.accessToken),
			);
			statusIn(res, [204, 404], 'cleanup-user-withdraw');
		}
	});
}

function findCardId(cardsBody, nickname, cardNumber) {
	if (!Array.isArray(cardsBody)) return null;
	const matched =
		cardsBody.find((card) => card.nickName === nickname && card.cardNumber === cardNumber) ||
		cardsBody.find((card) => card.cardNumber === cardNumber) ||
		cardsBody[0];
	return matched && Number.isFinite(matched.userCardId) ? matched.userCardId : null;
}

function findTravelId(travelListBody, travelPlaceName) {
	if (!Array.isArray(travelListBody)) return null;
	const matched =
		travelListBody.find((travel) => travel.travelPlaceName === travelPlaceName) || travelListBody[0];
	return matched && Number.isFinite(matched.travelId) ? matched.travelId : null;
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
	// Keep IDs short so derived merchant names stay within API max length(40).
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
