package com.genesis.unipocket.tempexpense.command.presentation;

import com.genesis.unipocket.tempexpense.command.presentation.response.TempExpenseConvertValidationErrorResponse;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = TemporaryExpenseCommandController.class)
public class TemporaryExpenseExceptionHandler {

	@ExceptionHandler(TempExpenseConvertValidationException.class)
	public ResponseEntity<TempExpenseConvertValidationErrorResponse>
			handleConvertValidationException(TempExpenseConvertValidationException e) {
		return ResponseEntity.status(e.getCode().getStatus())
				.body(TempExpenseConvertValidationErrorResponse.from(e));
	}
}
