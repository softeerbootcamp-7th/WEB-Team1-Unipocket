package com.genesis.unipocket.expense.query.port;

import com.genesis.unipocket.expense.query.port.dto.UserCardQueryInfo;

public interface UserCardReadService {
	UserCardQueryInfo readUserCard(Long userCardId);
}
