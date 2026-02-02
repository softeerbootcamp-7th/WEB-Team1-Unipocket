package com.genesis.unipocket.accountbook.presentation.dto.request;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <b>POST /api/account-books 입력</b>
 * @author bluefishez
 * @since 2026-01-30
 */
@Getter
public class CreateAccountBookReq {

	private String title;

	private CountryCode localCountryCode;

	private CountryCode baseCountryCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;
}
