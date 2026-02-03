package com.genesis.unipocket.user.command.persistence.entity;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "oauth_login_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OAuthLoginStateEntity {

	@Id
	@Column(name = "state_token", length = 255)
	private String state; // 변수명 유지 (Service 에러 방지)

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	private ProviderType providerType;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "is_used", nullable = false)
	private Boolean isUsed = false;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public OAuthLoginStateEntity(String state, ProviderType providerType, LocalDateTime expiresAt) {
		this.state = state;
		this.providerType = providerType;
		this.expiresAt = expiresAt;
		this.isUsed = false; // 생성 시 기본값
	}

	public void markAsUsed() {
		this.isUsed = true;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}
}
