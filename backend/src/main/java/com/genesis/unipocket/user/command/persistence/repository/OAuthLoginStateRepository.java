package com.genesis.unipocket.user.command.persistence.repository;

import com.genesis.unipocket.user.command.persistence.entity.OAuthLoginStateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <b>OAuth 로그인 State Repository</b>
 * @author 김동균
 * @since 2026-01-30
 */
public interface OAuthLoginStateRepository extends JpaRepository<OAuthLoginStateEntity, String> {

	Optional<OAuthLoginStateEntity> findByState(String state);
}
