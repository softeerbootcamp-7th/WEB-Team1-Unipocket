package com.genesis.unipocket.user.query.persistence.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * <b>로그인 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

	/**
	 * Access Token (JWT)
	 */
	private String accessToken;

	/**
	 * Refresh Token (JWT)
	 */
	private String refreshToken;

	/**
	 * 사용자 ID
	 */
	private UUID userId;

	/**
	 * Access Token 만료 시간 (초)
	 */
	private int expiresIn;

	/**
	 * 토큰 타입 (Bearer)
	 */
	private String tokenType;

	/**
	 * 정적 팩토리 메서드
	 */
	public static LoginResponse of(
			String accessToken, String refreshToken, UUID userId, int expiresIn) {
		return LoginResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.userId(userId)
				.expiresIn(expiresIn)
				.tokenType("Bearer")
				.build();
	}
}
