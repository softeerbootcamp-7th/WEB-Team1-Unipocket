package com.genesis.unipocket.accountbook.command.presentation.request;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AccountBookUpdateRequest(
		@NotNull(message = CODE) String title,
		@NotNull(message = CODE) CountryCode localCountryCode,
		@NotNull(message = CODE) CountryCode baseCountryCode,
		Long budget,
		@DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = CODE) LocalDate startDate,
		@DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = CODE) LocalDate endDate) {

	public static final String CODE = ErrorCode.CodeLiterals.ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED;
}
