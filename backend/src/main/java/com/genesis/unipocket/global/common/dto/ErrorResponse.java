package com.genesis.unipocket.global.common.dto;

import com.genesis.unipocket.global.exception.ErrorCode;

/**
 * <b>전역 에러 응답 객체</b>
 * <p>
 * 에러 반환 시 응답에 대한 형식을 설정하는 객체
 * </p>
 * @param code
 * @param message
 * @since 2026-01-25
 * @author bluefishez
 */
public record ErrorResponse(String code, String message) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
	}
}
