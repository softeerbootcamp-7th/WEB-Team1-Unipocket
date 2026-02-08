package com.genesis.unipocket.auth;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * <b>인증 컨텍스트</b>
 *
 * <p>
 * 현재 인증된 사용자 정보 조회
 *
 * <p>
 * TODO: JWT 인증 완성 후 실제 토큰에서 userId 추출
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Component
public class AuthenticationContext {

	// TODO: JWT/Spring Security 통합 후 실제 구현
	// 현재는 임시로 하드코딩된 사용자 ID 반환

	private static final String TEMP_USER_ID = "00000000-0000-0000-0000-000000000001";

	/**
	 * 현재 인증된 사용자 ID 조회
	 *
	 * @return 사용자 UUID
	 * @throws com.genesis.unipocket.global.exception.BusinessException (ErrorCode.UNAUTHORIZED) 인증되지 않은 경우
	 */
	public UUID getCurrentUserId() {
		// TODO: Spring Security Context에서 실제 사용자 조회
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// if (auth instanceof JwtAuthenticationToken token) {
		// return UUID.fromString(token.getName());
		// }

		// 임시: 하드코딩된 사용자 ID (개발용)
		return UUID.fromString(TEMP_USER_ID);
	}

	/**
	 * 인증 여부 확인
	 *
	 * @return 인증된 경우 true
	 */
	public boolean isAuthenticated() {
		// TODO: 실제 인증 상태 확인
		return true; // 임시로 항상 true
	}

	/**
	 * 사용자 ID를 String으로 반환
	 *
	 * @return 사용자 ID (String)
	 */
	public String getCurrentUserIdAsString() {
		return getCurrentUserId().toString();
	}
}
