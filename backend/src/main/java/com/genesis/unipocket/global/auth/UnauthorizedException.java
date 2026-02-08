package com.genesis.unipocket.global.auth;

/**
 * <b>인증 실패 예외</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public class UnauthorizedException extends RuntimeException {
	public UnauthorizedException(String message) {
		super(message);
	}
}
