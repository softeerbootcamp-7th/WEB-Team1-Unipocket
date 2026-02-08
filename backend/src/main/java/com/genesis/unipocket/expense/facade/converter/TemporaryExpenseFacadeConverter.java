package com.genesis.unipocket.expense.facade.converter;

import com.genesis.unipocket.expense.application.dto.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseUpdateRequest;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * <b>임시지출내역 Facade Converter</b>
 * <p>
 * Presentation DTO ↔ Application DTO, Entity → Response 변환
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Component
public class TemporaryExpenseFacadeConverter {

	/**
	 * Entity → Response DTO
	 */
	public TemporaryExpenseResponse toResponse(TemporaryExpense entity) {
		return new TemporaryExpenseResponse(
				entity.getTempExpenseId(),
				entity.getFileId(),
				entity.getMerchantName(),
				entity.getCategory(),
				entity.getLocalCountryCode(),
				entity.getLocalCurrencyAmount(),
				entity.getBaseCountryCode(),
				entity.getBaseCurrencyAmount(),
				entity.getPaymentsMethod(),
				entity.getMemo(),
				entity.getOccurredAt(),
				entity.getStatus() != null ? entity.getStatus().name() : null,
				entity.getCardLastFourDigits());
	}

	/**
	 * Entity List → Response DTO List
	 */
	public List<TemporaryExpenseResponse> toResponseList(List<TemporaryExpense> entities) {
		return entities.stream().map(this::toResponse).toList();
	}

	/**
	 * Presentation DTO → Application Command
	 */
	public TemporaryExpenseUpdateCommand toCommand(TemporaryExpenseUpdateRequest request) {
		return new TemporaryExpenseUpdateCommand(
				request.merchantName(),
				request.category(),
				request.localCountryCode(),
				request.localCurrencyAmount(),
				request.baseCountryCode(),
				request.baseCurrencyAmount(),
				request.paymentsMethod(),
				request.memo(),
				request.occurredAt(),
				request.cardLastFourDigits());
	}
}
