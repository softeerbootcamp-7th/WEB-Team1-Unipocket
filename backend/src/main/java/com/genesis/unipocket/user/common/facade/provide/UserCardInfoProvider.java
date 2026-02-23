package com.genesis.unipocket.user.common.facade.provide;

import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCardInfoProvider implements UserCardFetchService {

	private final UserCardCommandRepository userCardCommandRepository;

	@Override
	public Optional<UserCardInfo> getUserCard(Long userCardId) {
		if (userCardId == null) {
			return Optional.empty();
		}

		Optional<UserCardEntity> card = userCardCommandRepository.findById(userCardId);

		return card.map(
				userCardEntity ->
						new UserCardInfo(
								userCardEntity.getUserCardId(),
								userCardEntity.getCardCompany(),
								userCardEntity.getNickName(),
								userCardEntity.getCardNumber()));
	}

	@Override
	public Optional<UserCardInfo> getUserCardOwnedBy(Long userCardId, String userId) {
		if (userCardId == null || userId == null || userId.isBlank()) {
			return Optional.empty();
		}

		UUID ownerId;
		try {
			ownerId = UUID.fromString(userId);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}

		return userCardCommandRepository
				.findByUserCardIdAndUser_Id(userCardId, ownerId)
				.map(
						userCardEntity ->
								new UserCardInfo(
										userCardEntity.getUserCardId(),
										userCardEntity.getCardCompany(),
										userCardEntity.getNickName(),
										userCardEntity.getCardNumber()));
	}
}
