package com.genesis.unipocket.global.exception;

import lombok.Getter;

/**
 * <b>비즈니스 로직 예외</b>
 * <p>
 * 비즈니스 로직 수행도중 발생할 수 있는 예외들은 모두 해당 예외를 상속합니다.
 * </p>
 * @author bluefishez
 * @since 2026-01-25
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode code;

	public BusinessException(ErrorCode code) {
		super(code.getMessage());
		this.code = code;
	}
}
