package com.genesis.unipocket.expense.command.facade.port;

import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;

public interface UserCardFetchService {
	UserCardInfo getUserCard(Long userCardId);
}
