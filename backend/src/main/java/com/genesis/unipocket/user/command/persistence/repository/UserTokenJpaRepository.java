package com.genesis.unipocket.user.command.persistence.repository;

import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserTokenEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <b>사용자 토큰 JPA Repository</b>
 * <p>
 * 사용자 토큰 엔티티에 대한 데이터베이스 작업을 처리합니다.
 * </p>
 * @author 김동균
 * @since 2026-01-30
 */
public interface UserTokenJpaRepository extends JpaRepository<UserTokenEntity, Long> {

	/**
	 * 사용자로 토큰 조회 (무효화되지 않은 토큰)
	 */
	Optional<UserTokenEntity> findByUserAndIsRevokedFalse(UserEntity user);

	/**
	 * Refresh Token으로 조회
	 */
	Optional<UserTokenEntity> findByRefreshToken(String refreshToken);

	/**
	 * 만료된 토큰 삭제
	 */
	void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
