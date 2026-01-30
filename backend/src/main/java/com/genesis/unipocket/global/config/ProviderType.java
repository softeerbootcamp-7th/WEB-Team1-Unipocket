package com.genesis.unipocket.global.config; // 혹은 global.common.enums

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType {
    GOOGLE(true),
    KAKAO(true);

    private final boolean clientSecretRequired;
}