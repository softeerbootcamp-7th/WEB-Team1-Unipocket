package com.genesis.unipocket.expense.command.facade.port;

import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;

/**
 * <b>사용자 카드 정보 조회 포트</b>
 *
 * @author bluefishez
 * @since 2026-02-12
 */
public interface UserCardFetchService {
	UserCardInfo getUserCard(Long userCardId);
}
