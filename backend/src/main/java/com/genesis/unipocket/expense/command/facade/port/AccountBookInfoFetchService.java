package com.genesis.unipocket.expense.command.facade.port;

import com.genesis.unipocket.expense.common.dto.AccountBookInfo;

/**
 * <b>가계부 정보 조회 포트</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public interface AccountBookInfoFetchService {
	AccountBookInfo getAccountBook(Long accountBookId, String userId);
}
