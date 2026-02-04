package com.genesis.unipocket.accountbook.command.application.converter;

import com.genesis.unipocket.accountbook.command.application.dto.AccountBookCreateResult;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountBookApplicationConverter {

	@Mapping(source = "id", target = "accountBookId")
	AccountBookCreateResult toResult(AccountBookEntity entity);
}
