package com.genesis.unipocket.auth.command.persistence.repository;

import com.genesis.unipocket.auth.command.persistence.entity.SocialAuthEntity;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <b>소셜 인증 Repository</b>
 *
 * @author 김동균
 * @since 2026-01-30
 */
public interface SocialAuthRepository extends JpaRepository<SocialAuthEntity, Long> {

	/**
	 * Provider와 Provider ID로 소셜 인증 정보 조회
	 * 주의: 메서드명은 Entity의 필드명과 정확히 일치해야 함
	 * Entity 필드: private ProviderType provider;
	 */
	Optional<SocialAuthEntity> findByProviderAndProviderId(
			ProviderType provider, // 필드명: provider
			String providerId);

	boolean existsByProviderAndProviderId(
			ProviderType provider, // 필드명: provider
			String providerId);

	void deleteByUser(UserEntity user);

	void deleteByUser_Id(UUID userId);
}
