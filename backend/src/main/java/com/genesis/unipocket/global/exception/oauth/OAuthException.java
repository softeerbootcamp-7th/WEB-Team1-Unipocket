package com.genesis.unipocket.global.exception.oauth;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;

/**
 * <b>OAuth 관련 예외</b>
 * <p>
 * OAuth 인증 과정에서 발생하는 일반적인 예외를 처리합니다.
 * </p>
 * @author bluefishez
 * @since 2026-01-29
 */
public class OAuthException extends BusinessException {

    public OAuthException(ErrorCode code) {
        super(code);
    }
}