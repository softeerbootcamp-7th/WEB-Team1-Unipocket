package com.genesis.unipocket.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>토큰 재발급 요청 DTO</b>
 */
@Getter
@NoArgsConstructor
public class ReissueRequest {

	@NotBlank(message = "리프레시 토큰은 필수입니다.") private String refreshToken;
}
