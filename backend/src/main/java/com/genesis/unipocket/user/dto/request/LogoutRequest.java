package com.genesis.unipocket.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>로그아웃 요청 DTO</b>
 */
@Getter
@NoArgsConstructor
public class LogoutRequest {

	@NotBlank(message = "액세스 토큰은 필수입니다.") private String accessToken;

	@NotBlank(message = "리프레시 토큰은 필수입니다.") private String refreshToken;
}
