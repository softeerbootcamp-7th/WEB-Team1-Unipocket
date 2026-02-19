package com.genesis.unipocket.auth.command.persistence.entity;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * <b>소셜 인증 Entity</b>
 */
@Entity
@Table(name = "social_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SocialAuthEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "auth_id")
	private Long id; //

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user; //

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	private ProviderType provider; //

	@Column(name = "provider_id", nullable = false, length = 100)
	private String providerId; //

	@Column(name = "email", length = 100)
	private String email; //

	@CreatedDate
	@Column(name = "connected_at", nullable = false, updatable = false)
	private LocalDateTime createdAt; //

	@Builder
	public SocialAuthEntity(
			UserEntity user, ProviderType provider, String providerId, String email) {
		this.user = user;
		this.provider = provider;
		this.providerId = providerId;
		this.email = email;
	}
}
