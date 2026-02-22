package com.genesis.unipocket.tempexpense.command.facade.port;

import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;

public interface AccountBookRateInfoProvider {
	AccountBookRateInfo getRateInfo(Long accountBookId);
}
