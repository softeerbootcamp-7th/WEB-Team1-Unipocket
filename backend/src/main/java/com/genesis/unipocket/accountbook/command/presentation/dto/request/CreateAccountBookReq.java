package com.genesis.unipocket.accountbook.command.presentation.dto.request;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <b>POST /api/account-books 입력</b>
 * @author bluefishez
 * @since 2026-01-30
 */
@Data
public class CreateAccountBookReq {

	private final String title;

	private final CountryCode localCountryCode;

	private final CountryCode baseCountryCode;

	@DateTimeFormat(pattern = "yyyy-MM-DD")
	private final LocalDate startDate;

	@DateTimeFormat(pattern = "yyyy-MM-DD")
	private final LocalDate endDate;
}
