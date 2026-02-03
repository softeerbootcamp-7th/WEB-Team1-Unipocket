package com.genesis.unipocket.global.exception.oauth;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import lombok.Getter;

/**
 * <b>OAuth Provider 통신 예외</b>
 * <p>
 * OAuth Provider와의 HTTP 통신 중 발생하는 예외를 처리합니다.
 * </p>
 * @author 김동균
 * @since 2026-01-29
 */
@Getter
public class OAuthCommunicationException extends BusinessException {

	private final CommunicationType type;

	public OAuthCommunicationException(CommunicationType type) {
		super(getErrorCode(type));
		this.type = type;
	}

	private static ErrorCode getErrorCode(CommunicationType type) {
		return switch (type) {
			case TOKEN -> ErrorCode.OAUTH_TOKEN_REQUEST_FAILED;
			case USERINFO -> ErrorCode.OAUTH_USERINFO_REQUEST_FAILED;
		};
	}

	public enum CommunicationType {
		TOKEN,
		USERINFO
	}
}
