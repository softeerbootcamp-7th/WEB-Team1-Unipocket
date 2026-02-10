package com.genesis.unipocket.auth.command.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.global.exception.TokenException;
import com.genesis.unipocket.user.persistence.entity.UserEntity;
import com.genesis.unipocket.user.persistence.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

	@Mock private JwtProvider jwtProvider;
	@Mock private TokenBlacklistService blacklistService;
	@Mock private UserRepository userRepository;
	@InjectMocks private AuthService authService;

	@Test
	@DisplayName("로그인 - 성공")
	void login_Success() {
		UUID userId = UUID.randomUUID();
		UserEntity user = UserEntity.builder().name("Test User").email("test@example.com").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(jwtProvider.createAccessToken(userId)).thenReturn("access-token");
		when(jwtProvider.createRefreshToken(userId)).thenReturn("refresh-token");

		AuthService.TokenPair result = authService.login(userId);

		assertThat(result.accessToken()).isEqualTo("access-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
	}

	@Test
	@DisplayName("로그인 - 사용자 없음")
	void login_UserNotFound() {
		UUID userId = UUID.randomUUID();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(userId)).isInstanceOf(TokenException.class);
	}

	@Test
	@DisplayName("토큰 재발급 - 성공 (RTR 적용)")
	void reissue_Success() {
		String oldRefreshToken = "old-refresh-token";
		String oldJti = "old-jti-123";
		UUID userId = UUID.randomUUID();

		when(jwtProvider.validateToken(oldRefreshToken)).thenReturn(true);
		when(jwtProvider.getJti(oldRefreshToken)).thenReturn(oldJti);
		when(blacklistService.isBlacklisted(oldJti)).thenReturn(false);
		when(jwtProvider.getUserId(oldRefreshToken)).thenReturn(userId);
		when(jwtProvider.createAccessToken(userId)).thenReturn("new-access-token");
		when(jwtProvider.createRefreshToken(userId)).thenReturn("new-refresh-token");
		when(jwtProvider.getRemainingExpiration(oldRefreshToken)).thenReturn(60000L);

		AuthService.TokenPair result = authService.reissue(oldRefreshToken);

		assertThat(result.accessToken()).isEqualTo("new-access-token");
		assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
		verify(blacklistService).addToBlacklist(oldJti, 60000L);
	}

	@Test
	@DisplayName("토큰 재발급 - 유효하지 않은 토큰")
	void reissue_InvalidToken() {
		when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

		assertThatThrownBy(() -> authService.reissue("invalid-token"))
				.isInstanceOf(TokenException.class);
	}

	@Test
	@DisplayName("토큰 재발급 - 블랙리스트에 있는 토큰")
	void reissue_BlacklistedToken() {
		when(jwtProvider.validateToken("token")).thenReturn(true);
		when(jwtProvider.getJti("token")).thenReturn("jti");
		when(blacklistService.isBlacklisted("jti")).thenReturn(true);

		assertThatThrownBy(() -> authService.reissue("token")).isInstanceOf(TokenException.class);
	}

	@Test
	@DisplayName("로그아웃 - 성공")
	void logout_Success() {
		when(jwtProvider.getJti("at")).thenReturn("at-jti");
		when(jwtProvider.getRemainingExpiration("at")).thenReturn(1800000L);
		when(jwtProvider.getJti("rt")).thenReturn("rt-jti");
		when(jwtProvider.getRemainingExpiration("rt")).thenReturn(864000000L);

		authService.logout("at", "rt");

		verify(blacklistService).addToBlacklist("at-jti", 1800000L);
		verify(blacklistService).addToBlacklist("rt-jti", 864000000L);
	}
}
