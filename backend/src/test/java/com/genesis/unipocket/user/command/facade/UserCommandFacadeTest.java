package com.genesis.unipocket.user.command.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.application.UserCommandService;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.command.WithdrawUserCommand;
import com.genesis.unipocket.user.query.persistence.response.LoginResponse;
import com.genesis.unipocket.user.common.OAuthUserInfo;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCommandFacadeTest {

	@Mock
	private UserCommandService userCommandService;

	@InjectMocks
	private UserCommandFacade userCommandFacade;

	@Test
	@DisplayName("로그인 또는 회원가입 처리 - 성공")
	void loginOrRegister_Success() {
		// given
		OAuthUserInfo userInfo = org.mockito.Mockito.mock(OAuthUserInfo.class);
		ProviderType providerType = ProviderType.GOOGLE;
		LoginResponse expectedResponse = LoginResponse.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

		when(userCommandService.loginOrRegister(any(RegisterUserCommand.class)))
				.thenReturn(expectedResponse);

		// when
		userCommandFacade.loginOrRegister(userInfo, providerType);

		// then
		verify(userCommandService).loginOrRegister(any(RegisterUserCommand.class));
	}

	@Test
	@DisplayName("회원 탈퇴 처리 - 성공")
	void withdrawUser_Success() {
		// given
		UUID userId = UUID.randomUUID();

		// when
		userCommandFacade.withdraw(userId);

		// then
		verify(userCommandService).withdrawUser(any(WithdrawUserCommand.class));
	}
}
