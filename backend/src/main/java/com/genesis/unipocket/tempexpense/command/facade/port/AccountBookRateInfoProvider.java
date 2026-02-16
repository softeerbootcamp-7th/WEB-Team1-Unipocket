package com.genesis.unipocket.tempexpense.command.facade.port;

import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;

/**
 * <b>가계부 환율 컨텍스트 조회 포트</b>
 */
public interface AccountBookRateInfoProvider {
	AccountBookRateInfo getRateInfo(Long accountBookId);
}
