package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.global.auth.JwtProvider;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserTokenEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserJpaRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserTokenJpaRepository;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenCommandService {

	private final UserTokenJpaRepository userTokenRepository;
	private final UserJpaRepository userRepository;
	private final JwtProvider jwtProvider; // 🆕 기술 도구 주입

	@Value("${jwt.access-token-validity}")
	private long accessTokenValidityMs;

	@Value("${jwt.refresh-token-validity}")
	private long refreshTokenValidityMs;

	@Transactional
	public LoginResponse createTokens(Long userId) {
		// 1. 사용자 조회
		UserEntity user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 2. 기존 토큰 무효화
		userTokenRepository.findByUserAndIsRevokedFalse(user).ifPresent(UserTokenEntity::revoke);

		// 3. JWT 도구를 사용하여 토큰 문자열 생성
		String accessToken = jwtProvider.createToken(userId, accessTokenValidityMs);
		String refreshToken = jwtProvider.createToken(userId, refreshTokenValidityMs);

		// 4. DB에 Refresh Token 저장
		UserTokenEntity tokenEntity =
				UserTokenEntity.builder()
						.user(user)
						.refreshToken(refreshToken)
						.expiresAt(LocalDateTime.now().plusNanos(refreshTokenValidityMs * 1000000))
						.build();
		userTokenRepository.save(tokenEntity);

		log.info("JWT 토큰 발행 완료: userId={}", userId);

		return LoginResponse.of(accessToken, refreshToken, userId, accessTokenValidityMs / 1000);
	}
}
