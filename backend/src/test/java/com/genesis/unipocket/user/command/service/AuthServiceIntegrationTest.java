package com.genesis.unipocket.user.command.service;

import static org.assertj.core.api.Assertions.*;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.global.auth.JwtProvider;
import com.genesis.unipocket.global.auth.TokenBlacklistService;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService 통합 테스트 (실제 Redis 사용)
 */
@SpringBootTest
@Transactional
@TestPropertySource(
		properties = {"spring.data.redis.host=localhost", "spring.data.redis.port=6379"})
@DisplayName("AuthService 통합 테스트")
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test-it")
@Tag("integration")
class AuthServiceIntegrationTest {

	@Autowired private AuthService authService;
	@Autowired private JwtProvider jwtProvider;
	@Autowired private TokenBlacklistService blacklistService;
	@Autowired private UserRepository userRepository;

	@Test
	@DisplayName("전체 로그인 플로우 - 토큰 발급 및 검증")
	void fullLoginFlow() {
		// given - 사용자 생성
		UserEntity user = UserEntity.builder().name("Test User").email("test@example.com").build();
		userRepository.save(user);

		// when - 로그인
		AuthService.TokenPair tokens = authService.login(user.getId());

		// then
		assertThat(tokens.accessToken()).isNotNull();
		assertThat(tokens.refreshToken()).isNotNull();
		assertThat(jwtProvider.validateToken(tokens.accessToken())).isTrue();
		assertThat(jwtProvider.validateToken(tokens.refreshToken())).isTrue();
	}

	@Test
	@DisplayName("토큰 재발급 플로우 - RTR 검증")
	void tokenReissueFlow() throws InterruptedException {
		// given - 사용자 생성 및 로그인
		UserEntity user = UserEntity.builder().name("Test User").email("test@example.com").build();
		userRepository.save(user);

		AuthService.TokenPair originalTokens = authService.login(user.getId());
		String originalRT = originalTokens.refreshToken();
		String originalJti = jwtProvider.getJti(originalRT);

		// when - 토큰 재발급
		Thread.sleep(10); // 시간차 확보
		AuthService.TokenPair newTokens = authService.reissue(originalRT);

		// then - 새로운 토큰 발급 확인
		assertThat(newTokens.accessToken()).isNotEqualTo(originalTokens.accessToken());
		assertThat(newTokens.refreshToken()).isNotEqualTo(originalTokens.refreshToken());

		// 기존 RT가 블랙리스트에 등록되었는지 확인 (RTR)
		assertThat(blacklistService.isBlacklisted(originalJti)).isTrue();

		// 기존 RT로 재발급 시도 시 실패
		assertThatThrownBy(() -> authService.reissue(originalRT)).isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("로그아웃 플로우 - 블랙리스트 등록 검증")
	void logoutFlow() {
		// given - 사용자 생성 및 로그인
		UserEntity user = UserEntity.builder().name("Test User").email("test@example.com").build();
		userRepository.save(user);

		AuthService.TokenPair tokens = authService.login(user.getId());
		String atJti = jwtProvider.getJti(tokens.accessToken());
		String rtJti = jwtProvider.getJti(tokens.refreshToken());

		// when - 로그아웃
		authService.logout(tokens.accessToken(), tokens.refreshToken());

		// then - 블랙리스트 등록 확인
		assertThat(blacklistService.isBlacklisted(atJti)).isTrue();
		assertThat(blacklistService.isBlacklisted(rtJti)).isTrue();
	}
}
