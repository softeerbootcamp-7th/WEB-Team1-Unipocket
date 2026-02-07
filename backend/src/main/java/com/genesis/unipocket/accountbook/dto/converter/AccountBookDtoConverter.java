package com.genesis.unipocket.accountbook.dto.converter;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookResponse;
import com.genesis.unipocket.accountbook.dto.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.entity.AccountBookEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountBookDtoConverter {

	AccountBookDto toDto(AccountBookEntity entity);

	AccountBookResponse toResponse(AccountBookDto dto);

	AccountBookDetailResponse toDetailResponse(AccountBookDto dto, List<?> tempExpenseBatchIds);

	@Mapping(target = "isMain", ignore = true)
	default AccountBookSummaryResponse toSummaryResponse(
			AccountBookDto dto, Long mainAccountBookId) {
		return new AccountBookSummaryResponse(
				dto.id(), dto.title(), dto.id().equals(mainAccountBookId));
	}
}
