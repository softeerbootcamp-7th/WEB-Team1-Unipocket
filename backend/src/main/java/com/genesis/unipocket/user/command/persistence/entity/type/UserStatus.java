package com.genesis.unipocket.user.command.persistence.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    DELETED("탈퇴"),
    BANNED("정지");

    private final String description;
}