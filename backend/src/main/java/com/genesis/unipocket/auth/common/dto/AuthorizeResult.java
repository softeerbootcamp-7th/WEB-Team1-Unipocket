package com.genesis.unipocket.auth.common.dto;

/**
 * <b>OAuth 인증 결과 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
public record AuthorizeResult(String authorizationUrl, String state) {}
