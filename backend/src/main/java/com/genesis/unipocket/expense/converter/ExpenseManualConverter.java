package com.genesis.unipocket.expense.converter;

import com.genesis.unipocket.expense.dto.common.ExpenseDto;
import com.genesis.unipocket.expense.dto.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.dto.response.ExpenseManualCreateResponse;
import com.genesis.unipocket.expense.dto.response.ExpenseResponse;
import com.genesis.unipocket.expense.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.entity.expense.ExpenseEntity;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * <b>지출내역 관련 DTO Converter</b>
 * <ul>
 *     <li>Entity <-> DTO 변환
 *     <li>DTO <-> Response 변환
 * </ul>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Mapper(componentModel = "spring")
public interface ExpenseManualConverter {

	@Mapping(source = "expenseId", target = "id")
	@Mapping(target = "merchantName", source = "merchant.merchantName")
	@Mapping(target = "displayMerchantName", source = "merchant.displayMerchantName")
	@Mapping(target = "baseCurrencyCode", source = "exchangeInfo.baseCurrencyCode")
	@Mapping(target = "baseCurrencyAmount", source = "exchangeInfo.baseCurrencyAmount")
	@Mapping(target = "localCurrencyCode", source = "exchangeInfo.localCurrencyCode")
	@Mapping(target = "localCurrencyAmount", source = "exchangeInfo.localCurrencyAmount")
	@Mapping(target = "expenseSource", source = "expenseSourceInfo.expenseSource")
	@Mapping(target = "fileLink", source = "expenseSourceInfo.fileLink")
	ExpenseDto toDto(ExpenseEntity entity);

	ExpenseManualCreateArgs toManualArgs(
			ExpenseManualCreateRequest req,
			Long accountBookId,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount);

	default ExpenseManualCreateResponse toResponse(ExpenseDto dto) {
		return ExpenseManualCreateResponse.from(dto);
	}

	default ExpenseResponse toExpenseResponse(ExpenseDto dto) {
		return ExpenseResponse.from(dto);
	}
}
