package com.genesis.unipocket.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <b>API 응답 wrapper</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T data;
	private String message;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, message);
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, null, message);
	}
}
