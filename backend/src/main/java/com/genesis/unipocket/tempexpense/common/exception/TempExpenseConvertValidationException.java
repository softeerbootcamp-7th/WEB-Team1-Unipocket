package com.genesis.unipocket.tempexpense.common.exception;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.util.List;
import lombok.Getter;

@Getter
public class TempExpenseConvertValidationException extends BusinessException {

	private final List<Violation> violations;

	public TempExpenseConvertValidationException(List<Violation> violations) {
		super(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		this.violations = List.copyOf(violations);
	}

	public static TempExpenseConvertValidationException single(
			Long tempExpenseId, List<String> missingOrInvalidFields) {
		return new TempExpenseConvertValidationException(
				List.of(new Violation(tempExpenseId, List.copyOf(missingOrInvalidFields))));
	}

	public record Violation(Long tempExpenseId, List<String> missingOrInvalidFields) {}
}
