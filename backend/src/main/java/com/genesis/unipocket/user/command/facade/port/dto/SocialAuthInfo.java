package com.genesis.unipocket.user.command.facade.port.dto;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import java.util.UUID;

public record SocialAuthInfo(
		UUID userId, ProviderType providerType, String providerId, String email) {}
