package com.genesis.unipocket.auth.common.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * <b>로그인 결과 DTO</b>
 * <p>Auth 내부 공용
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Getter
@Builder
public class LoginResult {

	private final String accessToken;
	private final String refreshToken;
	private final Long expiresIn;

	public static LoginResult of(String accessToken, String refreshToken, Long expiresIn) {
		return LoginResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.expiresIn(expiresIn)
				.build();
	}
}
