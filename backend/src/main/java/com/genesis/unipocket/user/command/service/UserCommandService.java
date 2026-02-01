package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.persistence.entity.SocialAuthEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.SocialAuthJpaRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserJpaRepository;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>사용자 Command Service</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserJpaRepository userRepository;
    private final SocialAuthJpaRepository socialAuthRepository;
    private final UserTokenCommandService userTokenCommandService;

    @Transactional
    public LoginResponse loginOrRegister(OAuthUserInfo userInfo, ProviderType providerType) {
        // 1. 소셜 인증 정보로 사용자 조회
        SocialAuthEntity socialAuth = socialAuthRepository
                .findByProviderTypeAndProviderId(providerType, userInfo.getProviderId())
                .orElseGet(() -> {
                    // 2. 신규 사용자 생성
                    UserEntity newUser = UserEntity.builder()
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .build();
                    userRepository.save(newUser);

                    // 3. 소셜 인증 정보 저장
                    SocialAuthEntity newSocialAuth = SocialAuthEntity.builder()
                            .user(newUser)
                            .providerType(providerType)
                            .providerId(userInfo.getProviderId())
                            .build();
                    return socialAuthRepository.save(newSocialAuth);
                });

        UserEntity user = socialAuth.getUser();

        // 4. 기존 사용자인 경우 프로필 업데이트
        user.updateProfile(userInfo.getName(), userInfo.getProfileImageUrl());

        // 5. JWT 토큰 발급
        String accessToken = generateAccessToken(user);
        String refreshToken = userTokenCommandService.createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, user.getId());
    }

    private String generateAccessToken(UserEntity user) {
        // TODO: JWT 토큰 생성 로직 구현
        return "temp_access_token_" + user.getId();
    }
}