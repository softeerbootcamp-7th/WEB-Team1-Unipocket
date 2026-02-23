package com.genesis.unipocket.user.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import com.genesis.unipocket.user.command.application.result.UserCardUpdateResult;
import com.genesis.unipocket.user.command.facade.port.SocialAuthReadPort;
import com.genesis.unipocket.user.command.facade.port.SocialAuthWritePort;
import com.genesis.unipocket.user.command.facade.port.TokenIssuePort;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

	@Mock private UserCommandRepository userRepository;
	@Mock private UserCardCommandRepository userCardRepository;
	@Mock private SocialAuthReadPort socialAuthReadPort;
	@Mock private SocialAuthWritePort socialAuthWritePort;
	@Mock private TokenIssuePort tokenIssuePort;

	@InjectMocks private UserCommandService userCommandService;

	@Test
	@DisplayName("신규 OAuth 유저 생성 시 이름이 있으면 그대로 저장한다")
	void loginOrRegister_usesProvidedNameWhenPresent() {
		// given
		RegisterUserCommand command =
				RegisterUserCommand.of(
						ProviderType.KAKAO,
						"provider-id-1",
						"test@example.com",
						"given-name",
						"https://example.com/profile.png");
		UUID newUserId = UUID.randomUUID();
		LoginOrRegisterResult tokenResult = LoginOrRegisterResult.of("at", "rt", 1800L);

		when(socialAuthReadPort.findByProviderAndProviderId(ProviderType.KAKAO, "provider-id-1"))
				.thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class)))
				.thenAnswer(
						invocation -> {
							UserEntity saved = invocation.getArgument(0);
							ReflectionTestUtils.setField(saved, "id", newUserId);
							return saved;
						});
		when(tokenIssuePort.issueTokens(newUserId)).thenReturn(tokenResult);

		// when
		LoginOrRegisterResult result = userCommandService.loginOrRegister(command);

		// then
		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		verify(userRepository).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getName()).isEqualTo("given-name");
		assertThat(result).isEqualTo(tokenResult);
	}

	@Test
	@DisplayName("신규 OAuth 유저 생성 시 이름이 없으면 랜덤 fallback 이름을 저장한다")
	void loginOrRegister_generatesFallbackNameWhenMissing() {
		// given
		RegisterUserCommand command =
				RegisterUserCommand.of(
						ProviderType.KAKAO,
						"provider-id-2",
						null,
						" ",
						"https://example.com/profile.png");
		UUID newUserId = UUID.randomUUID();
		LoginOrRegisterResult tokenResult = LoginOrRegisterResult.of("at2", "rt2", 1800L);

		when(socialAuthReadPort.findByProviderAndProviderId(ProviderType.KAKAO, "provider-id-2"))
				.thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class)))
				.thenAnswer(
						invocation -> {
							UserEntity saved = invocation.getArgument(0);
							ReflectionTestUtils.setField(saved, "id", newUserId);
							return saved;
						});
		when(tokenIssuePort.issueTokens(newUserId)).thenReturn(tokenResult);

		// when
		LoginOrRegisterResult result = userCommandService.loginOrRegister(command);

		// then
		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		verify(userRepository).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getName()).matches("user_[0-9a-f]{8}");
		assertThat(result).isEqualTo(tokenResult);
	}

	@Test
	@DisplayName("카드 닉네임 수정 성공")
	void updateCard_success() {
		UUID userId = UUID.randomUUID();
		Long cardId = 1L;
		UserEntity owner = UserEntity.reference(userId);
		UserCardEntity userCard =
				UserCardEntity.builder()
						.user(owner)
						.nickName("Before")
						.cardNumber("1234")
						.cardCompany(CardCompany.HYUNDAI)
						.build();
		ReflectionTestUtils.setField(userCard, "userCardId", cardId);

		when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));

		UserCardUpdateResult result = userCommandService.updateCard(userId, cardId, "After");

		assertThat(result.nickName()).isEqualTo("After");
		assertThat(result.userCardId()).isEqualTo(cardId);
		verify(userCardRepository).save(userCard);
	}

	@Test
	@DisplayName("카드 닉네임 수정 실패 - 카드 소유자가 아님")
	void updateCard_failWhenNotOwner() {
		UUID ownerId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		Long cardId = 1L;
		UserEntity owner = UserEntity.reference(ownerId);
		UserCardEntity userCard =
				UserCardEntity.builder()
						.user(owner)
						.nickName("Before")
						.cardNumber("1234")
						.cardCompany(CardCompany.HYUNDAI)
						.build();

		when(userCardRepository.findById(cardId)).thenReturn(Optional.of(userCard));

		assertThatThrownBy(() -> userCommandService.updateCard(requesterId, cardId, "After"))
				.isInstanceOfSatisfying(
						BusinessException.class,
						ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CARD_NOT_OWNED));

		verify(userCardRepository, never()).save(any());
	}

	@Test
	@DisplayName("카드 닉네임 수정 실패 - 카드 없음")
	void updateCard_failWhenCardNotFound() {
		UUID userId = UUID.randomUUID();
		Long cardId = 1L;
		when(userCardRepository.findById(cardId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userCommandService.updateCard(userId, cardId, "After"))
				.isInstanceOfSatisfying(
						BusinessException.class,
						ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CARD_NOT_FOUND));

		verify(userCardRepository, never()).save(any());
	}
}
