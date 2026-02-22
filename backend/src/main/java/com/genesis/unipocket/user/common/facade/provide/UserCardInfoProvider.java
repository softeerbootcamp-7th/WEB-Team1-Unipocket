package com.genesis.unipocket.user.common.facade.provide;

import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import java.util.Optional;
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
}
