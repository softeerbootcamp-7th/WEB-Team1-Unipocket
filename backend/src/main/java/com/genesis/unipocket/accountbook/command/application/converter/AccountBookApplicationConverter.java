package com.genesis.unipocket.accountbook.command.application.converter;

import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateCommand;
import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateResult;
import com.genesis.unipocket.accountbook.command.persistence.dto.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountBookApplicationConverter {

	@Mapping(source = "id", target = "accountBookId")
	AccountBookCreateResult toResult(AccountBookEntity entity);

	AccountBookCreateArgs toArgs(
			AccountBookCreateCommand command, CountryCode baseCountryCode, String title);
}
