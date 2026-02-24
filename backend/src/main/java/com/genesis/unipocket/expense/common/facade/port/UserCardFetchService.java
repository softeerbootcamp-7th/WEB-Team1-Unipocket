package com.genesis.unipocket.expense.common.facade.port;

import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import java.util.Optional;

public interface UserCardFetchService {
	Optional<UserCardInfo> getUserCard(Long userCardId);

	Optional<UserCardInfo> getUserCardOwnedBy(Long userCardId, String userId);
}
