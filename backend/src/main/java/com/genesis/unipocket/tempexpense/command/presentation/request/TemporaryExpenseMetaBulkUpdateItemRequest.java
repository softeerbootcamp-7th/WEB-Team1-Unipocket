package com.genesis.unipocket.tempexpense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public record TemporaryExpenseMetaBulkUpdateItemRequest(
		@NotNull Long tempExpenseId,
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String memo,
		LocalDateTime occurredAt,
		Optional<@Pattern(regexp = "\\d{4}", message = "카드 번호 뒷자리는 4자리 숫자여야 합니다.") String>
				cardLastFourDigits) {}
