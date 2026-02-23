import analysisWorkflow from './analysis-country-dirty.k6.js';
import tempExpenseWorkflow from './tempexpense-image-workflow.k6.js';
import widgetWorkflow from './widget-query-hotpath.k6.js';

const RUN_ANALYSIS = toBoolean(__ENV.RUN_ANALYSIS, true);
const RUN_TEMPEXPENSE = toBoolean(__ENV.RUN_TEMPEXPENSE, true);
const RUN_WIDGET = toBoolean(__ENV.RUN_WIDGET, true);

const scenarios = {};

if (RUN_ANALYSIS) {
	scenarios.analysis = createScenario('ANALYSIS', {
		startVUs: 1,
		stage1Duration: '1m',
		stage1Target: 3,
		stage2Duration: '2m',
		stage2Target: 10,
		stage3Duration: '1m',
		stage3Target: 0,
		gracefulRampDown: '20s',
		exec: 'runAnalysis',
	});
}

if (RUN_TEMPEXPENSE) {
	scenarios.tempexpense = createScenario('TEMPEXPENSE', {
		startVUs: 1,
		stage1Duration: '1m',
		stage1Target: 2,
		stage2Duration: '2m',
		stage2Target: 8,
		stage3Duration: '1m',
		stage3Target: 0,
		gracefulRampDown: '20s',
		exec: 'runTempExpense',
	});
}

if (RUN_WIDGET) {
	scenarios.widget = createScenario('WIDGET', {
		startVUs: 1,
		stage1Duration: '1m',
		stage1Target: 5,
		stage2Duration: '3m',
		stage2Target: 20,
		stage3Duration: '1m',
		stage3Target: 0,
		gracefulRampDown: '20s',
		exec: 'runWidget',
	});
}

if (Object.keys(scenarios).length === 0) {
	scenarios.widget = createScenario('WIDGET', {
		startVUs: 1,
		stage1Duration: '30s',
		stage1Target: 1,
		stage2Duration: '30s',
		stage2Target: 1,
		stage3Duration: '10s',
		stage3Target: 0,
		gracefulRampDown: '10s',
		exec: 'runWidget',
	});
}

export const options = {
	scenarios,
	thresholds: {
		http_req_failed: ['rate<0.1'],
		checks: ['rate>0.9'],
	},
};

export function runAnalysis() {
	analysisWorkflow();
}

export function runTempExpense() {
	tempExpenseWorkflow();
}

export function runWidget() {
	widgetWorkflow();
}

function createScenario(prefix, defaults) {
	const stage1Duration = __ENV[`${prefix}_STAGE_1_DURATION`] || defaults.stage1Duration;
	const stage2Duration = __ENV[`${prefix}_STAGE_2_DURATION`] || defaults.stage2Duration;
	const stage3Duration = __ENV[`${prefix}_STAGE_3_DURATION`] || defaults.stage3Duration;

	return {
		executor: 'ramping-vus',
		exec: defaults.exec,
		startVUs: toNumber(__ENV[`${prefix}_START_VUS`], defaults.startVUs),
		gracefulRampDown:
			__ENV[`${prefix}_GRACEFUL_RAMP_DOWN`] || defaults.gracefulRampDown,
		stages: [
			{
				duration: stage1Duration,
				target: toNumber(__ENV[`${prefix}_STAGE_1_TARGET`], defaults.stage1Target),
			},
			{
				duration: stage2Duration,
				target: toNumber(__ENV[`${prefix}_STAGE_2_TARGET`], defaults.stage2Target),
			},
			{
				duration: stage3Duration,
				target: toNumber(__ENV[`${prefix}_STAGE_3_TARGET`], defaults.stage3Target),
			},
		],
		tags: {
			domain: prefix.toLowerCase(),
		},
	};
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
