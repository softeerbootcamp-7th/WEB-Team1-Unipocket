package com.genesis.unipocket.expense.command.facade.port.dto;

import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;

/**
 * <b>사용자 카드 정보 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-12
 */
public record UserCardInfo(
		Long userCardId, CardCompany cardCompany, String nickName, String cardNumber) {}
