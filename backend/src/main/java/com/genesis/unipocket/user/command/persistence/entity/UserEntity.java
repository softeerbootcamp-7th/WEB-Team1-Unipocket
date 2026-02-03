package com.genesis.unipocket.user.command.persistence.entity;

import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * <b>사용자 Entity</b>
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "profile_img_url", length = 255)
    private String profileImgUrl;

    // String -> UserRole Enum으로 변경
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.ROLE_USER;

    // String -> UserStatus Enum으로 변경
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "main_bucket_id", nullable = false)
    private Long mainBucketId = 0L;

    @Builder
    public UserEntity(String email, String name, String profileImgUrl, Long mainBucketId, UserRole role, UserStatus status) {
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
        this.mainBucketId = (mainBucketId != null) ? mainBucketId : 0L;
        // 생성 시점에 권한과 상태를 명시하고 싶을 경우를 위해 빌더에 추가 (기본값 세팅 포함)
        this.role = (role != null) ? role : UserRole.ROLE_USER;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
    }

    public void updateProfile(String name, String profileImgUrl) {
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

    // 비즈니스 로직 수정: Enum 상수를 사용하도록 변경
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}