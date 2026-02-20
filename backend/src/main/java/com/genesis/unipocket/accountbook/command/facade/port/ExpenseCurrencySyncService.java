package com.genesis.unipocket.accountbook.command.facade.port;

import com.genesis.unipocket.global.common.enums.CurrencyCode;

public interface ExpenseCurrencySyncService {

	void updateBaseCurrency(Long accountBookId, CurrencyCode currencyCode);
}
