package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.persistence.entity.SocialAuthEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.SocialAuthJpaRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserJpaRepository;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>사용자 Command Service</b>
 * <p>
 * 사용자 생성, 수정, 삭제 등의 Command 작업을 처리합니다.
 * </p>
 * @author 김동균
 * @since 2026-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserJpaRepository userRepository;
	private final SocialAuthJpaRepository socialAuthRepository;
	private final UserTokenCommandService userTokenCommandService;

	/**
	 * OAuth 로그인 또는 회원가입 처리
	 *
	 * @param userInfo OAuth 사용자 정보
	 * @param providerType OAuth Provider 타입
	 * @return 로그인 응답 (JWT 토큰)
	 */
	@Transactional
	public LoginResponse loginOrRegister(OAuthUserInfo userInfo, ProviderType providerType) {
		// 1. 소셜 인증 정보 조회 또는 생성
		SocialAuthEntity socialAuth =
				socialAuthRepository
						.findByProviderAndProviderId(providerType, userInfo.getProviderId())
						.orElseGet(() -> createNewUser(userInfo, providerType));

		// 2. 사용자 정보 가져오기
		UserEntity user = socialAuth.getUser();

		// 3. JWT 토큰 생성
		return userTokenCommandService.createTokens(user.getId());
	}

	/**
	 * 새로운 사용자 생성
	 *
	 * @param userInfo OAuth 사용자 정보
	 * @param providerType OAuth Provider 타입
	 * @return 생성된 소셜 인증 정보
	 */
	private SocialAuthEntity createNewUser(OAuthUserInfo userInfo, ProviderType providerType) {
		log.info(
				"Creating new user from OAuth: provider={}, providerId={}",
				providerType,
				userInfo.getProviderId());

		// 사용자 엔티티 생성
		UserEntity user =
				UserEntity.builder()
						.email(userInfo.getEmail())
						.name(userInfo.getName())
						.profileImgUrl(userInfo.getProfileImageUrl())
						.build();

		userRepository.save(user);

		// 소셜 인증 정보 생성
		SocialAuthEntity socialAuth =
				SocialAuthEntity.builder()
						.user(user)
						.provider(providerType)
						.email(userInfo.getEmail())
						.providerId(userInfo.getProviderId())
						.build();

		return socialAuthRepository.save(socialAuth);
	}
}
