package com.genesis.unipocket.accountbook.command.facade.converter;

import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateCommand;
import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateResult;
import com.genesis.unipocket.accountbook.command.presentation.dto.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.dto.response.AccountBookCreateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountBookFacadeConverter {

	AccountBookCreateCommand toCommand(
			String userId, String username, AccountBookCreateRequest req);

	@Mapping(source = "accountBookId", target = "id")
	AccountBookCreateResponse toRes(AccountBookCreateResult result);
}
