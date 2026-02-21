package com.genesis.unipocket.expense.command.facade.port;

import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;

public interface AccountBookFetchService {
	AccountBookInfo getAccountBook(Long accountBookId, String userId);
}
