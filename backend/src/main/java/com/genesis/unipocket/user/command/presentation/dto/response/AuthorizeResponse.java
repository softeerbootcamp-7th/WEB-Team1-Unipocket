package com.genesis.unipocket.user.command.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <b>OAuth 인증 URL 응답 DTO</b>
 * 
 * @author 김동균
 * @since 2026-01-30
 */
@Getter
@AllArgsConstructor
public class AuthorizeResponse {

	private String authorizationUrl;

	private String state;
}
