package com.genesis.unipocket.accountbook.query.service;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateSource;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.port.ExchangeRateReader;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountBookQueryService {

	private final AccountBookQueryRepository accountBookQueryRepository;
	private final ExchangeRateReader exchangeRateReader;

	public AccountBookQueryResponse getAccountBook(Long accountBookId) {
		return accountBookQueryRepository
				.findById(accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	public AccountBookDetailResponse getAccountBookDetail(String userId, Long accountBookId) {
		UUID userUuid = UUID.fromString(userId);
		return accountBookQueryRepository
				.findDetailById(userUuid, accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	public AccountBookExchangeRateResponse getAccountBookExchangeRate(
			String userId, Long accountBookId, LocalDateTime occurredAt) {
		UUID userUuid = UUID.fromString(userId);
		AccountBookExchangeRateSource accountBookExchangeRateSource =
				accountBookQueryRepository
						.findExchangeRateSourceById(userUuid, accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));

		CurrencyCode baseCurrencyCode =
				accountBookExchangeRateSource.baseCountryCode().getCurrencyCode();
		CurrencyCode localCurrencyCode =
				accountBookExchangeRateSource.localCountryCode().getCurrencyCode();
		OffsetDateTime quotedAt =
				(occurredAt != null ? occurredAt : LocalDateTime.now()).atOffset(ZoneOffset.UTC);
		var exchangeRate =
				exchangeRateReader.getExchangeRate(baseCurrencyCode, localCurrencyCode, quotedAt);

		return new AccountBookExchangeRateResponse(
				accountBookExchangeRateSource.baseCountryCode(),
				accountBookExchangeRateSource.localCountryCode(),
				exchangeRate,
				accountBookExchangeRateSource.budgetCreatedAt());
	}

	public List<AccountBookSummaryResponse> getAccountBooks(String userId) {
		UUID userUuid = UUID.fromString(userId);
		return accountBookQueryRepository.findAllByUserId(userUuid);
	}
}
