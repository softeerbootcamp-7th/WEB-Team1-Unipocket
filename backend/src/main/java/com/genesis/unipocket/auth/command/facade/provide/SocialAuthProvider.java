package com.genesis.unipocket.auth.command.facade.provide;

import com.genesis.unipocket.auth.command.persistence.entity.SocialAuthEntity;
import com.genesis.unipocket.auth.command.persistence.repository.SocialAuthRepository;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.facade.port.SocialAuthReadPort;
import com.genesis.unipocket.user.command.facade.port.SocialAuthWritePort;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthCreate;
import com.genesis.unipocket.user.command.facade.port.dto.SocialAuthInfo;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthProvider implements SocialAuthReadPort, SocialAuthWritePort {

	private final SocialAuthRepository socialAuthRepository;
	private final UserCommandRepository userRepository;

	@Override
	public Optional<SocialAuthInfo> findByProviderAndProviderId(
			ProviderType providerType, String providerId) {
		return socialAuthRepository
				.findByProviderAndProviderId(providerType, providerId)
				.map(this::toInfo);
	}

	@Override
	public SocialAuthInfo save(SocialAuthCreate command) {
		UserEntity user =
				userRepository
						.findById(command.userId())
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		SocialAuthEntity entity =
				SocialAuthEntity.builder()
						.user(user)
						.provider(command.providerType())
						.providerId(command.providerId())
						.email(command.email())
						.build();

		SocialAuthEntity saved = socialAuthRepository.save(entity);
		return toInfo(saved);
	}

	@Override
	public void deleteByUserId(UUID userId) {
		socialAuthRepository.deleteByUser_Id(userId);
	}

	private SocialAuthInfo toInfo(SocialAuthEntity entity) {
		return new SocialAuthInfo(
				entity.getUser().getId(),
				entity.getProvider(),
				entity.getProviderId(),
				entity.getEmail());
	}
}
