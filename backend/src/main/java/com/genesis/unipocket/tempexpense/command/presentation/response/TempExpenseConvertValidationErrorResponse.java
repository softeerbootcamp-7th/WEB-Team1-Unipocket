package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import java.util.List;

public record TempExpenseConvertValidationErrorResponse(
		String code, String message, List<ViolationResponse> violations) {

	public static TempExpenseConvertValidationErrorResponse from(
			TempExpenseConvertValidationException exception) {
		List<ViolationResponse> violations =
				exception.getViolations().stream()
						.map(
								violation ->
										new ViolationResponse(
												violation.tempExpenseId(),
												violation.missingOrInvalidFields()))
						.toList();
		return new TempExpenseConvertValidationErrorResponse(
				exception.getCode().getCode(), exception.getMessage(), violations);
	}

	public record ViolationResponse(Long tempExpenseId, List<String> missingOrInvalidFields) {}
}
