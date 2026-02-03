package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.global.auth.JwtProvider;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.repository.UserRepository;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenCommandService {

	private final UserRepository userRepository;
	private final JwtProvider jwtProvider; // 🆕 기술 도구 주입

	@Value("${jwt.access-token-validity}")
	private long accessTokenValidityMs;

	@Transactional
	public LoginResponse createTokens(UUID userId) {
		// 1. 사용자 존재 여부 확인 (필요하다면)
		if (!userRepository.existsById(userId)) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}

		// 2. JWT 도구를 사용하여 토큰 문자열 생성
		String accessToken = jwtProvider.createToken(userId, accessTokenValidityMs);

		log.info("JWT 토큰 발행 완료: userId={}", userId);

		return LoginResponse.of(accessToken, userId, accessTokenValidityMs / 1000);
	}
}
