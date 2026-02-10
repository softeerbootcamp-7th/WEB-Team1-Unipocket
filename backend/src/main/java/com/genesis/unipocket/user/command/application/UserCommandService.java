package com.genesis.unipocket.user.command.application;

import com.genesis.unipocket.auth.persistence.entity.SocialAuthEntity;
import com.genesis.unipocket.auth.persistence.repository.SocialAuthRepository;
import com.genesis.unipocket.auth.service.TokenService;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.application.command.CreateCardCommand;
import com.genesis.unipocket.user.command.application.command.DeleteCardCommand;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.command.WithdrawUserCommand;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.user.common.dto.oauth.OAuthUserInfo;
import com.genesis.unipocket.user.query.persistence.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserCommandRepository userRepository;
	private final SocialAuthRepository socialAuthRepository;
	private final UserCardCommandRepository userCardRepository;
	private final TokenService tokenService;

	@Transactional
	public LoginResponse loginOrRegister(RegisterUserCommand command) {
		OAuthUserInfo userInfo = command.userInfo();
		ProviderType providerType = command.providerType();

		SocialAuthEntity socialAuth =
				socialAuthRepository
						.findByProviderAndProviderId(providerType, userInfo.getProviderId())
						.orElseGet(() -> createNewUser(userInfo, providerType));

		UserEntity user = socialAuth.getUser();
		return tokenService.createTokens(user.getId());
	}

	private SocialAuthEntity createNewUser(OAuthUserInfo userInfo, ProviderType providerType) {
		log.info(
				"Creating new user from OAuth: provider={}, providerId={}",
				providerType,
				userInfo.getProviderId());

		UserEntity user =
				UserEntity.builder()
						.email(userInfo.getEmail())
						.name(userInfo.getName())
						.profileImgUrl(userInfo.getProfileImageUrl())
						.build();

		userRepository.save(user);

		SocialAuthEntity socialAuth =
				SocialAuthEntity.builder()
						.user(user)
						.provider(providerType)
						.email(userInfo.getEmail())
						.providerId(userInfo.getProviderId())
						.build();

		return socialAuthRepository.save(socialAuth);
	}

	@Transactional
	public void withdrawUser(WithdrawUserCommand command) {
		UserEntity user =
				userRepository
						.findById(command.userId())
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		socialAuthRepository.deleteByUser(user);
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
