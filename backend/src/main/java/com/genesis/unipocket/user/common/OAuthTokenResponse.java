package com.genesis.unipocket.user.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>OAuth Token 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Getter
@NoArgsConstructor
public class OAuthTokenResponse {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("expires_in")
	private Integer expiresIn;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("scope")
	private String scope;

	@JsonProperty("id_token")
	private String idToken;
}
