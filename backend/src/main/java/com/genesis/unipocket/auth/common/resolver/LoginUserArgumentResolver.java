package com.genesis.unipocket.auth.common.resolver;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	private final JwtProvider jwtProvider;
	private final TokenBlacklistService blacklistService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// @LoginUser 어노테이션이 붙어 있고, 타입이 UUID인 경우에만 동작
		return parameter.hasParameterAnnotation(LoginUser.class)
				&& parameter.getParameterType().equals(UUID.class);
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {

		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		// 1. 쿠키에서 토큰 추출
		String token = extractTokenFromCookie(request);
		if (token == null) {
			throw new BusinessException(ErrorCode.TOKEN_REQUIRED); // 적절한 CustomException으로 변경 권장
		}

		// 2. 토큰 유효성 검증 (만료 여부 등)
		if (!jwtProvider.validateToken(token)) {
			throw new BusinessException(ErrorCode.TOKEN_INVALID);
		}

		// 3. 블랙리스트 확인 (로그아웃 여부)
		String jti = jwtProvider.getJti(token);
		if (blacklistService.isBlacklisted(jti)) {
			throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED);
		}

		// 4. 토큰에서 User ID(UUID) 추출 및 반환
		String userId = String.valueOf(jwtProvider.getUserId(token));
		return UUID.fromString(userId);
	}

	private String extractTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) return null;

		return Arrays.stream(request.getCookies())
				.filter(cookie -> "access_token".equals(cookie.getName()))
				.map(Cookie::getValue)
				.findFirst()
				.orElse(null);
	}
}
