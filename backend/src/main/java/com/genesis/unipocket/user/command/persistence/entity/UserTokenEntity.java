package com.genesis.unipocket.user.command.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserTokenEntity implements Persistable<Long> {

	@Id
	@Column(name = "user_id")
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "refresh_token", nullable = false, length = 512)
	private String refreshToken;

	@Column(name = "expired_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "is_revoked", nullable = false)
	private Boolean isRevoked = false;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	public UserTokenEntity(
			UserEntity user, String refreshToken, String browser, LocalDateTime expiresAt) {
		this.user = user;
		this.refreshToken = refreshToken;
		this.expiresAt = expiresAt;
		this.isRevoked = false;
		if (user != null) {
			this.id = user.getId();
		}
	}

	// Persistable 인터페이스 구현 (StaleObjectStateException 해결 핵심)
	@Override
	public boolean isNew() {
		return createdAt == null; // 생성일자가 없으면 새로운 객체로 판단
	}

	public void revoke() {
		this.isRevoked = true;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}
