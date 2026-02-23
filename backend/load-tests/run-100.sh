#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PROFILE="${PROFILE:-master}" # master|workflow|widget|analysis|tempexpense
BASE_URL="${BASE_URL:-http://localhost:8080}"
TOTAL_TARGET="${TOTAL_TARGET:-100}"
START_VUS="${START_VUS:-50}"
RAMP_UP_DURATION="${RAMP_UP_DURATION:-10m}"
HOLD_DURATION="${HOLD_DURATION:-20m}"
RAMP_DOWN_DURATION="${RAMP_DOWN_DURATION:-10m}"
CLEANUP="${CLEANUP:-true}"
DEBUG_FAILURES="${DEBUG_FAILURES:-true}"
K6_OUT="${K6_OUT:-}"

RUN_ANALYSIS="${RUN_ANALYSIS:-true}"
RUN_WIDGET="${RUN_WIDGET:-true}"
RUN_TEMPEXPENSE="${RUN_TEMPEXPENSE:-false}"
ANALYSIS_SHARE="${ANALYSIS_SHARE:-30}" # master profile only
TEMPEXPENSE_SHARE="${TEMPEXPENSE_SHARE:-0}" # master profile only

usage() {
	cat <<'EOF'
Usage:
  PROFILE=master ./backend/load-tests/run-1000.sh

Profiles:
  master      unipocket-master.k6.js (multi scenario)
  workflow    unipocket-workflow.k6.js
  widget      widget-query-hotpath.k6.js
  analysis    analysis-country-dirty.k6.js
  tempexpense tempexpense-image-workflow.k6.js

Common env:
  BASE_URL=http://localhost:8080
  TOTAL_TARGET=1000
  START_VUS=50
  RAMP_UP_DURATION=10m
  HOLD_DURATION=20m
  RAMP_DOWN_DURATION=10m
  CLEANUP=true
  DEBUG_FAILURES=true
  K6_OUT=

Master env:
  RUN_ANALYSIS=true
  RUN_WIDGET=true
  RUN_TEMPEXPENSE=false
  ANALYSIS_SHARE=30
  TEMPEXPENSE_SHARE=0
EOF
}

require_k6() {
	if ! command -v k6 >/dev/null 2>&1; then
		echo "k6 command not found"
		exit 1
	fi
}

assert_uint() {
	local name="$1"
	local value="$2"
	if [[ ! "${value}" =~ ^[0-9]+$ ]]; then
		echo "${name} must be an unsigned integer, got: ${value}"
		exit 1
	fi
}

half_target() {
	local target="$1"
	if ((target <= 0)); then
		echo "0"
		return
	fi
	local half=$((target / 2))
	if ((half < 1)); then
		echo "1"
		return
	fi
	echo "${half}"
}

default_start_vus() {
	local target="$1"
	if ((target <= 0)); then
		echo "1"
		return
	fi
	if ((target < 20)); then
		echo "${target}"
		return
	fi
	echo "20"
}

run_k6() {
	local script_path="$1"
	echo "[run-1000] profile=${PROFILE} base_url=${BASE_URL} script=${script_path}"
	if [[ -n "${K6_OUT}" ]]; then
		k6 run --out "${K6_OUT}" "${script_path}"
	else
		k6 run "${script_path}"
	fi
}

run_master() {
	local analysis_target=0
	local tempexpense_target=0
	local widget_target=0

	if [[ "${RUN_ANALYSIS}" == "true" ]]; then
		analysis_target=$((TOTAL_TARGET * ANALYSIS_SHARE / 100))
	fi
	if [[ "${RUN_TEMPEXPENSE}" == "true" ]]; then
		tempexpense_target=$((TOTAL_TARGET * TEMPEXPENSE_SHARE / 100))
	fi

	if [[ "${RUN_WIDGET}" == "true" ]]; then
		widget_target=$((TOTAL_TARGET - analysis_target - tempexpense_target))
	else
		widget_target=0
	fi

	if ((widget_target < 0)); then
		echo "invalid share: ANALYSIS_SHARE + TEMPEXPENSE_SHARE exceeded 100"
		exit 1
	fi

	if ((analysis_target + tempexpense_target + widget_target != TOTAL_TARGET)); then
		echo "invalid targets: enabled scenarios do not sum to TOTAL_TARGET=${TOTAL_TARGET}"
		echo "set RUN_WIDGET=true or adjust ANALYSIS_SHARE/TEMPEXPENSE_SHARE"
		exit 1
	fi

	export BASE_URL
	export RUN_ANALYSIS
	export RUN_WIDGET
	export RUN_TEMPEXPENSE
	export CLEANUP
	export DEBUG_FAILURES

	if [[ "${RUN_ANALYSIS}" == "true" ]]; then
		export ANALYSIS_START_VUS="${ANALYSIS_START_VUS:-$(default_start_vus "${analysis_target}")}"
		export ANALYSIS_STAGE_1_DURATION="${ANALYSIS_STAGE_1_DURATION:-${RAMP_UP_DURATION}}"
		export ANALYSIS_STAGE_1_TARGET="${ANALYSIS_STAGE_1_TARGET:-$(half_target "${analysis_target}")}"
		export ANALYSIS_STAGE_2_DURATION="${ANALYSIS_STAGE_2_DURATION:-${HOLD_DURATION}}"
		export ANALYSIS_STAGE_2_TARGET="${ANALYSIS_STAGE_2_TARGET:-${analysis_target}}"
		export ANALYSIS_STAGE_3_DURATION="${ANALYSIS_STAGE_3_DURATION:-${RAMP_DOWN_DURATION}}"
		export ANALYSIS_STAGE_3_TARGET="${ANALYSIS_STAGE_3_TARGET:-0}"
	fi

	if [[ "${RUN_WIDGET}" == "true" ]]; then
		export WIDGET_START_VUS="${WIDGET_START_VUS:-$(default_start_vus "${widget_target}")}"
		export WIDGET_STAGE_1_DURATION="${WIDGET_STAGE_1_DURATION:-${RAMP_UP_DURATION}}"
		export WIDGET_STAGE_1_TARGET="${WIDGET_STAGE_1_TARGET:-$(half_target "${widget_target}")}"
		export WIDGET_STAGE_2_DURATION="${WIDGET_STAGE_2_DURATION:-${HOLD_DURATION}}"
		export WIDGET_STAGE_2_TARGET="${WIDGET_STAGE_2_TARGET:-${widget_target}}"
		export WIDGET_STAGE_3_DURATION="${WIDGET_STAGE_3_DURATION:-${RAMP_DOWN_DURATION}}"
		export WIDGET_STAGE_3_TARGET="${WIDGET_STAGE_3_TARGET:-0}"
	fi

	if [[ "${RUN_TEMPEXPENSE}" == "true" ]]; then
		export TEMPEXPENSE_START_VUS="${TEMPEXPENSE_START_VUS:-$(default_start_vus "${tempexpense_target}")}"
		export TEMPEXPENSE_STAGE_1_DURATION="${TEMPEXPENSE_STAGE_1_DURATION:-${RAMP_UP_DURATION}}"
		export TEMPEXPENSE_STAGE_1_TARGET="${TEMPEXPENSE_STAGE_1_TARGET:-$(half_target "${tempexpense_target}")}"
		export TEMPEXPENSE_STAGE_2_DURATION="${TEMPEXPENSE_STAGE_2_DURATION:-${HOLD_DURATION}}"
		export TEMPEXPENSE_STAGE_2_TARGET="${TEMPEXPENSE_STAGE_2_TARGET:-${tempexpense_target}}"
		export TEMPEXPENSE_STAGE_3_DURATION="${TEMPEXPENSE_STAGE_3_DURATION:-${RAMP_DOWN_DURATION}}"
		export TEMPEXPENSE_STAGE_3_TARGET="${TEMPEXPENSE_STAGE_3_TARGET:-0}"
	fi

	echo "[run-1000] master targets analysis=${analysis_target} widget=${widget_target} tempexpense=${tempexpense_target}"
	run_k6 "${SCRIPT_DIR}/unipocket-master.k6.js"
}

run_single_profile() {
	local script_path="$1"
	local enable_temp_expense="${2:-false}"

	export BASE_URL
	export CLEANUP
	export DEBUG_FAILURES
	export START_VUS
	export STAGE_1_DURATION="${STAGE_1_DURATION:-${RAMP_UP_DURATION}}"
	export STAGE_1_TARGET="${STAGE_1_TARGET:-$(half_target "${TOTAL_TARGET}")}"
	export STAGE_2_DURATION="${STAGE_2_DURATION:-${HOLD_DURATION}}"
	export STAGE_2_TARGET="${STAGE_2_TARGET:-${TOTAL_TARGET}}"
	export STAGE_3_DURATION="${STAGE_3_DURATION:-${RAMP_DOWN_DURATION}}"
	export STAGE_3_TARGET="${STAGE_3_TARGET:-0}"
	export ENABLE_TEMP_EXPENSE="${ENABLE_TEMP_EXPENSE:-${enable_temp_expense}}"

	run_k6 "${script_path}"
}

main() {
	if [[ "${1:-}" == "--help" ]]; then
		usage
		exit 0
	fi

	require_k6
	assert_uint "TOTAL_TARGET" "${TOTAL_TARGET}"
	assert_uint "START_VUS" "${START_VUS}"
	assert_uint "ANALYSIS_SHARE" "${ANALYSIS_SHARE}"
	assert_uint "TEMPEXPENSE_SHARE" "${TEMPEXPENSE_SHARE}"

	case "${PROFILE}" in
	master)
		run_master
		;;
	workflow)
		run_single_profile "${SCRIPT_DIR}/unipocket-workflow.k6.js" "false"
		;;
	widget)
		run_single_profile "${SCRIPT_DIR}/widget-query-hotpath.k6.js"
		;;
	analysis)
		run_single_profile "${SCRIPT_DIR}/analysis-country-dirty.k6.js"
		;;
	tempexpense)
		run_single_profile "${SCRIPT_DIR}/tempexpense-image-workflow.k6.js" "true"
		;;
	*)
		echo "unknown PROFILE: ${PROFILE}"
		usage
		exit 1
		;;
	esac
}

main "$@"
