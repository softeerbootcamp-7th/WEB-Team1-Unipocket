package com.genesis.unipocket.tempexpense.common.util;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;

public final class TemporaryExpenseTaskSupport {

	private TemporaryExpenseTaskSupport() {}

	public static int toPercent(int completed, int total) {
		if (total <= 0) {
			return 100;
		}
		return (completed * 100) / total;
	}

	public static String resolveClientErrorMessage(Exception e, ErrorCode fallbackCode) {
		if (e instanceof BusinessException businessException) {
			return businessException.getCode().getMessage();
		}
		return fallbackCode.getMessage();
	}

	public static RuntimeException rethrow(Exception e) {
		if (e instanceof RuntimeException runtimeException) {
			return runtimeException;
		}
		return new IllegalStateException(e);
	}
}
