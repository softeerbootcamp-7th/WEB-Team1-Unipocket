package com.genesis.unipocket.analysis.query.persistence.response;

public record CompareWithAverageRes(
		int month, String mySpentAmount, String averageSpentAmount, String spentAmountDiff) {}
