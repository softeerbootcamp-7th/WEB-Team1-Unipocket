package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.expense.command.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>사용자 카드 정보 Provider</b>
 *
 * @author bluefishez
 * @since 2026-02-12
 */
@Component
@RequiredArgsConstructor
public class UserCardInfoProvider implements UserCardFetchService {

	private final UserCardCommandRepository userCardCommandRepository;

	@Override
	public UserCardInfo getUserCard(Long userCardId) {
		UserCardEntity card =
				userCardCommandRepository
						.findById(userCardId)
						.orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

		return new UserCardInfo(
				card.getUserCardId(),
				card.getCardCompany(),
				card.getNickName(),
				card.getCardNumber());
	}
}
