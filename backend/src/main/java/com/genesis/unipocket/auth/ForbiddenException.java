package com.genesis.unipocket.auth;

/**
 * <b>권한 없음 예외</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public class ForbiddenException extends RuntimeException {
	public ForbiddenException(String message) {
		super(message);
	}
}
