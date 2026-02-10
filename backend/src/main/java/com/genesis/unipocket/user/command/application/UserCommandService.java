package com.genesis.unipocket.user.command.application;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.application.command.CreateCardCommand;
import com.genesis.unipocket.user.command.application.command.DeleteCardCommand;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.command.WithdrawUserCommand;
import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import com.genesis.unipocket.user.command.facade.port.SocialAuthReadPort;
import com.genesis.unipocket.user.command.facade.port.SocialAuthWritePort;
import com.genesis.unipocket.user.command.facade.port.TokenIssuePort;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthCreate;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthInfo;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserCommandRepository userRepository;
	private final UserCardCommandRepository userCardRepository;
	private final SocialAuthReadPort socialAuthReadPort;
	private final SocialAuthWritePort socialAuthWritePort;
	private final TokenIssuePort tokenIssuePort;

	@Transactional
	public LoginOrRegisterResult loginOrRegister(RegisterUserCommand command) {
		ProviderType providerType = command.providerType();
		String providerId = command.providerId();

		UUID userId =
				socialAuthReadPort
						.findByProviderAndProviderId(providerType, providerId)
						.map(SocialAuthInfo::userId)
						.orElseGet(() -> createNewUser(command));

		return tokenIssuePort.issueTokens(userId);
	}

	private UUID createNewUser(RegisterUserCommand command) {
		ProviderType providerType = command.providerType();
		String providerId = command.providerId();

		log.info(
				"Creating new user from OAuth: provider={}, providerId={}",
				providerType,
				providerId);

		UserEntity user =
				UserEntity.builder()
						.email(command.email())
						.name(command.name())
						.profileImgUrl(command.profileImageUrl())
						.build();

		userRepository.save(user);

		socialAuthWritePort.save(
				SocialAuthCreate.of(user.getId(), providerType, providerId, command.email()));

		return user.getId();
	}

	@Transactional
	public void withdrawUser(WithdrawUserCommand command) {
		UserEntity user =
				userRepository
						.findById(command.userId())
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		socialAuthWritePort.deleteByUserId(user.getId());
		userRepository.delete(user);
	}

	@Transactional
	public Long createCard(CreateCardCommand command) {
		UserEntity user =
				userRepository
						.findById(command.userId())
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		long cardCount = userCardRepository.countByUser(user);
		if (cardCount >= 10) {
			throw new BusinessException(ErrorCode.CARD_LIMIT_EXCEEDED);
		}

		UserCardEntity userCard =
				UserCardEntity.builder()
						.user(user)
						.nickName(command.nickName())
						.cardNumber(command.cardNumber())
						.cardCompany(command.cardCompany())
						.build();
		userCardRepository.save(userCard);
		return userCard.getUserCardId();
	}

	@Transactional
	public void deleteCard(DeleteCardCommand command) {
		UserCardEntity userCard =
				userCardRepository
						.findById(command.cardId())
						.orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

		if (!userCard.getUser().getId().equals(command.userId())) {
			throw new BusinessException(ErrorCode.CARD_NOT_OWNED);
		}

		userCardRepository.delete(userCard);
	}
}
