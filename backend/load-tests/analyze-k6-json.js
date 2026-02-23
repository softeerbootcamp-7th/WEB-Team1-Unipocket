#!/usr/bin/env node
'use strict';

const fs = require('fs');
const readline = require('readline');

function usage() {
	console.log(
		[
			'Usage:',
			'  node backend/load-tests/analyze-k6-json.js <k6-json-path> [topN]',
			'',
			'Example:',
			'  node backend/load-tests/analyze-k6-json.js /tmp/k6-workflow-100.json 20',
		].join('\n'),
	);
}

function quantile(sorted, q) {
	if (!sorted.length) return 0;
	const index = Math.ceil(sorted.length * q) - 1;
	const clamped = Math.max(0, Math.min(sorted.length - 1, index));
	return sorted[clamped];
}

function toFixed(num) {
	return Number.isFinite(num) ? num.toFixed(2) : '0.00';
}

function detectFlow(endpointName) {
	if (endpointName === 'health-check') return 'health';
	if (endpointName.startsWith('dev-')) return 'dev-auth';
	if (endpointName.startsWith('users-')) return 'user-card';
	if (endpointName.startsWith('travels-')) return 'travel';
	if (endpointName.startsWith('expenses-')) return 'expense';
	if (endpointName.startsWith('analysis-')) return 'analysis';
	if (endpointName.startsWith('cleanup-')) return 'cleanup';
	if (endpointName.startsWith('temp-expense')) return 'temp-expense';
	if (
		endpointName.startsWith('account-books-widget') ||
		endpointName.startsWith('account-books-widgets') ||
		endpointName.startsWith('travel-widget') ||
		endpointName.startsWith('travel-widgets')
	) {
		return 'widget';
	}
	if (endpointName.startsWith('account-books-')) return 'account-book';
	return 'other';
}

async function main() {
	const filePath = process.argv[2];
	const topNInput = Number(process.argv[3] || 20);
	const topN = Number.isFinite(topNInput) && topNInput > 0 ? Math.floor(topNInput) : 20;

	if (!filePath) {
		usage();
		process.exit(1);
	}
	if (!fs.existsSync(filePath)) {
		console.error(`File not found: ${filePath}`);
		process.exit(1);
	}

	const endpointStats = new Map();
	const uniqueNameTags = new Set();
	const uniqueUrlTags = new Set();

	function getStats(endpointName) {
		if (!endpointStats.has(endpointName)) {
			endpointStats.set(endpointName, {
				endpointName,
				count: 0,
				sumDuration: 0,
				maxDuration: 0,
				durations: [],
				failSamples: 0,
				failCount: 0,
			});
		}
		return endpointStats.get(endpointName);
	}

	const lineReader = readline.createInterface({
		input: fs.createReadStream(filePath),
		crlfDelay: Infinity,
	});

	for await (const line of lineReader) {
		let parsed;
		try {
			parsed = JSON.parse(line);
		} catch {
			continue;
		}

		if (parsed.type !== 'Point' || !parsed.data || !parsed.data.tags) continue;
		const tags = parsed.data.tags;
		const endpointName = tags.name || tags.url || 'unknown';
		const value = Number(parsed.data.value);
		if (!Number.isFinite(value)) continue;

		if (tags.name) uniqueNameTags.add(tags.name);
		if (tags.url) uniqueUrlTags.add(tags.url);

		if (parsed.metric === 'http_req_duration') {
			const stats = getStats(endpointName);
			stats.count += 1;
			stats.sumDuration += value;
			if (value > stats.maxDuration) stats.maxDuration = value;
			stats.durations.push(value);
		} else if (parsed.metric === 'http_req_failed') {
			const stats = getStats(endpointName);
			stats.failSamples += 1;
			if (value > 0) stats.failCount += 1;
		}
	}

	const rows = [];
	for (const stats of endpointStats.values()) {
		if (!stats.count) continue;
		stats.durations.sort((a, b) => a - b);
		const avgDuration = stats.sumDuration / stats.count;
		const p90 = quantile(stats.durations, 0.9);
		const p95 = quantile(stats.durations, 0.95);
		const p99 = quantile(stats.durations, 0.99);
		const failRate = stats.failSamples > 0 ? stats.failCount / stats.failSamples : 0;

		rows.push({
			endpointName: stats.endpointName,
			count: stats.count,
			avgDuration,
			p90,
			p95,
			p99,
			maxDuration: stats.maxDuration,
			failCount: stats.failCount,
			failRate,
		});
	}

	rows.sort((a, b) => b.count - a.count);
	const totalRequests = rows.reduce((acc, row) => acc + row.count, 0);

	const byP95 = [...rows].sort((a, b) => b.p95 - a.p95);
	const byP99 = [...rows].sort((a, b) => b.p99 - a.p99);
	const byAvg = [...rows].sort((a, b) => b.avgDuration - a.avgDuration);
	const byVolume = [...rows].sort((a, b) => b.count - a.count);

	function printTable(title, tableRows) {
		console.log(`\n=== ${title} ===`);
		console.log(
			'endpoint\tcount\tfailRate\tavg_ms\tp90_ms\tp95_ms\tp99_ms\tmax_ms',
		);
		for (const row of tableRows.slice(0, topN)) {
			console.log(
				[
					row.endpointName,
					row.count,
					`${toFixed(row.failRate * 100)}%`,
					toFixed(row.avgDuration),
					toFixed(row.p90),
					toFixed(row.p95),
					toFixed(row.p99),
					toFixed(row.maxDuration),
				].join('\t'),
			);
		}
	}

	const flowStats = new Map();
	for (const row of rows) {
		const flow = detectFlow(row.endpointName);
		if (!flowStats.has(flow)) {
			flowStats.set(flow, {
				flow,
				endpoints: 0,
				totalAvgMsPerIteration: 0,
				totalFailCount: 0,
			});
		}
		const f = flowStats.get(flow);
		f.endpoints += 1;
		f.totalAvgMsPerIteration += row.avgDuration;
		f.totalFailCount += row.failCount;
	}

	const flowRows = [...flowStats.values()].sort(
		(a, b) => b.totalAvgMsPerIteration - a.totalAvgMsPerIteration,
	);

	console.log('=== k6 JSON Analysis ===');
	console.log(`file: ${filePath}`);
	console.log(`endpoints: ${rows.length}`);
	console.log(`total_http_requests: ${totalRequests}`);
	console.log(`unique_name_tags: ${uniqueNameTags.size}`);
	console.log(`unique_url_tags: ${uniqueUrlTags.size}`);

	console.log('\n=== Flow Contribution (avg ms per iteration) ===');
	console.log('flow\tendpoints\tavg_ms_per_iteration\tfail_count');
	for (const flow of flowRows) {
		console.log(
			`${flow.flow}\t${flow.endpoints}\t${toFixed(flow.totalAvgMsPerIteration)}\t${flow.totalFailCount}`,
		);
	}

	printTable('Top Endpoints By p95', byP95);
	printTable('Top Endpoints By p99', byP99);
	printTable('Top Endpoints By Average Latency', byAvg);
	printTable('Top Endpoints By Request Volume', byVolume);
}

main().catch((error) => {
	console.error(error);
	process.exit(1);
});
