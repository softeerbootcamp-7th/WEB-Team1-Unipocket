package com.genesis.unipocket.global.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * <b>토큰 재발급 요청</b>
 */
public record ReissueRequest(@NotBlank(message = "리프레시 토큰은 필수입니다.") String refreshToken) {}
