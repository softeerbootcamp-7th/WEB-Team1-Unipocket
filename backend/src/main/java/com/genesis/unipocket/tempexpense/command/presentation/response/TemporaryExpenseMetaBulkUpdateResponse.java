package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseMetaBulkUpdateResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TemporaryExpenseMetaBulkUpdateResponse(
		int totalRequested, int successCount, int failedCount, List<ItemResult> results) {

	public static TemporaryExpenseMetaBulkUpdateResponse from(
			TemporaryExpenseMetaBulkUpdateResult result) {
		List<ItemResult> mappedResults = result.results().stream().map(ItemResult::from).toList();
		return new TemporaryExpenseMetaBulkUpdateResponse(
				result.totalRequested(),
				result.successCount(),
				result.failedCount(),
				mappedResults);
	}

	public record ItemResult(
			Long tempExpenseId, String status, String reason, UpdatedExpense updated) {

		public static ItemResult from(TemporaryExpenseMetaBulkUpdateResult.ItemResult item) {
			return new ItemResult(
					item.tempExpenseId(),
					item.status(),
					item.reason(),
					item.updated() != null ? UpdatedExpense.from(item.updated()) : null);
		}
	}

	public record UpdatedExpense(
			Long tempExpenseId,
			Long tempExpenseMetaId,
			Long fileId,
			String merchantName,
			Category category,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			BigDecimal baseCurrencyAmount,
			String paymentsMethod,
			String memo,
			LocalDateTime occurredAt,
			String status,
			String cardLastFourDigits) {

		public static UpdatedExpense from(TemporaryExpenseResult result) {
			return new UpdatedExpense(
					result.tempExpenseId(),
					result.tempExpenseMetaId(),
					result.fileId(),
					result.merchantName(),
					result.category(),
					result.localCountryCode(),
					result.localCurrencyAmount(),
					result.baseCountryCode(),
					result.baseCurrencyAmount(),
					result.paymentsMethod(),
					result.memo(),
					result.occurredAt(),
					result.status(),
					result.cardLastFourDigits());
		}
	}
}
