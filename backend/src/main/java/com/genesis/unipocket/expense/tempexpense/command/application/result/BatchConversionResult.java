package com.genesis.unipocket.expense.tempexpense.command.application.result;

import java.util.List;

public record BatchConversionResult(
		int totalRequested, int successCount, int failedCount, List<ConversionResult> results) {}
