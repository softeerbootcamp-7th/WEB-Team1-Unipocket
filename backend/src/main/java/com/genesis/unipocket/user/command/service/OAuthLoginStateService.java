package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.oauth.OAuthException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.OAuthLoginStateEntity;
import com.genesis.unipocket.user.command.persistence.repository.OAuthLoginStateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <b>OAuth 로그인 State Service</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginStateService {

    private final OAuthLoginStateJpaRepository loginStateRepository;

    @Transactional
    public void saveLoginState(String state, ProviderType providerType) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        OAuthLoginStateEntity entity = OAuthLoginStateEntity.builder()
                .state(state)
                .providerType(providerType)
                .expiresAt(expiresAt)
                .build();

        loginStateRepository.save(entity);
    }

    @Transactional
    public void validateState(String state, ProviderType providerType) {
        OAuthLoginStateEntity loginState = loginStateRepository.findByState(state)
                .orElseThrow(() -> new OAuthException(ErrorCode.INVALID_OAUTH_STATE));

        if (loginState.isExpired()) {
            throw new OAuthException(ErrorCode.OAUTH_STATE_EXPIRED);
        }

        if (loginState.getIsUsed()) {
            throw new OAuthException(ErrorCode.INVALID_OAUTH_STATE);
        }

        if (!loginState.getProviderType().equals(providerType)) {
            throw new OAuthException(ErrorCode.INVALID_OAUTH_STATE);
        }

        loginState.markAsUsed();
    }
}