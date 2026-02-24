package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.global.common.enums.Category;
import java.math.BigDecimal;
import java.util.Map;

record AnalysisMyCategorySnapshot(
		Map<Category, BigDecimal> categoryMap, boolean monthlyBatchReady) {}
