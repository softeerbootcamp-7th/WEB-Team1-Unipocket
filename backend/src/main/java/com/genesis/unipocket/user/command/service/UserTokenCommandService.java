package com.genesis.unipocket.user.command.service;

import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserTokenEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <b>사용자 토큰 Command Service</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class UserTokenCommandService {

    private final UserTokenJpaRepository userTokenRepository;

    @Transactional
    public String createRefreshToken(UserEntity user) {
        // 기존 리프레시 토큰 무효화
        userTokenRepository.findByUserAndIsRevokedFalse(user)
                .ifPresent(UserTokenEntity::revoke);

        // 새 리프레시 토큰 생성
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        UserTokenEntity tokenEntity = UserTokenEntity.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();

        userTokenRepository.save(tokenEntity);

        return refreshToken;
    }
}