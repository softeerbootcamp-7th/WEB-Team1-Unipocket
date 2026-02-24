package com.genesis.unipocket.accountbook.command.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountBookUpdateRequest {

	public static final String CODE = ErrorCode.CodeLiterals.ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED;
	private static final int MAX_TITLE_LENGTH = 255;
	private static final int MAX_BUDGET_INTEGER_DIGITS = 17;
	private static final int MAX_BUDGET_FRACTION_DIGITS = 2;

	@NotBlank @Size(max = 30) private String title;

	private CountryCode localCountryCode;
	private CountryCode baseCountryCode;
	private BigDecimal budget;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean isMain;

	private boolean titlePresent;
	private boolean localCountryCodePresent;
	private boolean baseCountryCodePresent;
	private boolean budgetPresent;
	private boolean startDatePresent;
	private boolean endDatePresent;
	private boolean isMainPresent;

	public AccountBookUpdateRequest(
			String title,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			BigDecimal budget,
			LocalDate startDate,
			LocalDate endDate) {
		this(title, localCountryCode, baseCountryCode, budget, startDate, endDate, null);
		this.isMainPresent = false;
	}

	public AccountBookUpdateRequest(
			String title,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			BigDecimal budget,
			LocalDate startDate,
			LocalDate endDate,
			Boolean isMain) {
		this.title = title;
		this.localCountryCode = localCountryCode;
		this.baseCountryCode = baseCountryCode;
		this.budget = budget;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isMain = isMain;
		this.titlePresent = true;
		this.localCountryCodePresent = true;
		this.baseCountryCodePresent = true;
		this.budgetPresent = true;
		this.startDatePresent = true;
		this.endDatePresent = true;
		this.isMainPresent = true;
	}

	@JsonSetter("title")
	public void setTitle(String title) {
		this.title = title;
		this.titlePresent = true;
	}

	@JsonSetter("localCountryCode")
	public void setLocalCountryCode(CountryCode localCountryCode) {
		this.localCountryCode = localCountryCode;
		this.localCountryCodePresent = true;
	}

	@JsonSetter("baseCountryCode")
	public void setBaseCountryCode(CountryCode baseCountryCode) {
		this.baseCountryCode = baseCountryCode;
		this.baseCountryCodePresent = true;
	}

	@JsonSetter("budget")
	public void setBudget(BigDecimal budget) {
		this.budget = budget;
		this.budgetPresent = true;
	}

	@JsonSetter("startDate")
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
		this.startDatePresent = true;
	}

	@JsonSetter("endDate")
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
		this.endDatePresent = true;
	}

	@JsonSetter("isMain")
	public void setIsMain(Boolean isMain) {
		this.isMain = isMain;
		this.isMainPresent = true;
	}

	@JsonIgnore
	public String title() {
		return title;
	}

	@JsonIgnore
	public CountryCode localCountryCode() {
		return localCountryCode;
	}

	@JsonIgnore
	public CountryCode baseCountryCode() {
		return baseCountryCode;
	}

	@JsonIgnore
	public BigDecimal budget() {
		return budget;
	}

	@JsonIgnore
	public LocalDate startDate() {
		return startDate;
	}

	@JsonIgnore
	public LocalDate endDate() {
		return endDate;
	}

	@JsonIgnore
	public Boolean isMain() {
		return isMain;
	}

	@JsonIgnore
	public boolean titlePresent() {
		return titlePresent;
	}

	@JsonIgnore
	public boolean localCountryCodePresent() {
		return localCountryCodePresent;
	}

	@JsonIgnore
	public boolean baseCountryCodePresent() {
		return baseCountryCodePresent;
	}

	@JsonIgnore
	public boolean budgetPresent() {
		return budgetPresent;
	}

	@JsonIgnore
	public boolean startDatePresent() {
		return startDatePresent;
	}

	@JsonIgnore
	public boolean endDatePresent() {
		return endDatePresent;
	}

	@JsonIgnore
	public boolean isMainPresent() {
		return isMainPresent;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean hasPatchField() {
		return titlePresent
				|| localCountryCodePresent
				|| baseCountryCodePresent
				|| budgetPresent
				|| startDatePresent
				|| endDatePresent
				|| isMainPresent;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean isTitleValid() {
		if (!titlePresent) {
			return true;
		}
		return title != null && !title.isBlank() && title.length() <= MAX_TITLE_LENGTH;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean isCountryCodeValid() {
		if (localCountryCodePresent && localCountryCode == null) {
			return false;
		}
		if (baseCountryCodePresent && baseCountryCode == null) {
			return false;
		}
		return true;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean isDateValid() {
		if (startDatePresent && startDate == null) {
			return false;
		}
		if (endDatePresent && endDate == null) {
			return false;
		}
		return true;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean isIsMainValid() {
		if (!isMainPresent) {
			return true;
		}
		return isMain != null;
	}

	@AssertTrue(message = CODE) @JsonIgnore
	public boolean isBudgetValid() {
		if (!budgetPresent || budget == null) {
			return true;
		}
		if (budget.compareTo(BigDecimal.ZERO) < 0) {
			return false;
		}

		int scale = Math.max(0, budget.scale());
		if (scale > MAX_BUDGET_FRACTION_DIGITS) {
			return false;
		}

		int integerDigits = Math.max(0, budget.precision() - scale);
		return integerDigits <= MAX_BUDGET_INTEGER_DIGITS;
	}
}
