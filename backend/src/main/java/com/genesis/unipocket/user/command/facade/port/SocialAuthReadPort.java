package com.genesis.unipocket.user.command.facade.port;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthInfo;
import java.util.Optional;

public interface SocialAuthReadPort {

	Optional<SocialAuthInfo> findByProviderAndProviderId(
			ProviderType providerType, String providerId);
}
