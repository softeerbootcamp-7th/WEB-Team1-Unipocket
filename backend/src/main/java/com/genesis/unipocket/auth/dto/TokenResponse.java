package com.genesis.unipocket.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>토큰 응답</b>
 */
public record TokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("token_type") String tokenType) {

	public TokenResponse(String accessToken, String refreshToken) {
		this(accessToken, refreshToken, "Bearer");
	}
}
