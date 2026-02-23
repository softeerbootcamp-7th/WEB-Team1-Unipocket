package com.genesis.unipocket.analysis.query.application;

import java.math.BigDecimal;

record AnalysisPairPeerContext(
		boolean peerAvailable, BigDecimal avgTotal, long effectivePeerCount, boolean myIncluded) {

	static AnalysisPairPeerContext unavailable() {
		return new AnalysisPairPeerContext(false, null, 0L, false);
	}

	static AnalysisPairPeerContext available(
			BigDecimal avgTotal, long effectivePeerCount, boolean myIncluded) {
		return new AnalysisPairPeerContext(true, avgTotal, effectivePeerCount, myIncluded);
	}
}
