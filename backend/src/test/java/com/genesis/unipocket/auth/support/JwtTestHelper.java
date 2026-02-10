package com.genesis.unipocket.auth.support;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * 통합 테스트에서 JWT 인증을 쉽게 사용하기 위한 헬퍼 클래스
 */
@Component
public class JwtTestHelper {

	public static final String USER_ID_STR = "00000000-0000-0000-0000-000000000001";

	private final JwtProvider jwtProvider;

	public JwtTestHelper(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	public RequestPostProcessor withJwtAuth() {
		return withJwtAuth(UUID.fromString(USER_ID_STR));
	}

	public RequestPostProcessor withJwtAuth(UUID userId) {
		return request -> {
			String accessToken = jwtProvider.createAccessToken(userId);
			Cookie cookie = new Cookie("access_token", accessToken);
			cookie.setHttpOnly(true);
			cookie.setPath("/");
			request.setCookies(cookie);
			return request;
		};
	}

	public RequestPostProcessor withJwtAuth(String userIdStr) {
		return withJwtAuth(UUID.fromString(userIdStr));
	}
}
