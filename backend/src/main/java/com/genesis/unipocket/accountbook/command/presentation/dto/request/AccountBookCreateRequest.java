package com.genesis.unipocket.accountbook.command.presentation.dto.request;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AccountBookCreateRequest(
		@NotNull(message = CODE) CountryCode localCountryCode,
		@DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = CODE) LocalDate startDate,
		@DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = CODE) LocalDate endDate) {

	public static final String CODE = ErrorCode.CodeLiterals.ACCOUNT_BOOK_CREATE_VALIDATION_FAILED;
}
