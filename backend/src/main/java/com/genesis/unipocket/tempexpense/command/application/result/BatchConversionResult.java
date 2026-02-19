package com.genesis.unipocket.tempexpense.command.application.result;

import java.util.List;

public record BatchConversionResult(
		int totalRequested, int successCount, int failedCount, List<ConversionResult> results) {}
