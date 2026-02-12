package com.genesis.unipocket.accountbook.query.service;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountBookQueryService {

	private final AccountBookQueryRepository repository;
	private final ExchangeRateService exchangeRateService;
	private final UserCommandRepository userRepository;

	public AccountBookQueryResponse getAccountBook(Long accountBookId) {
		return repository
				.findById(accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	public AccountBookDetailResponse getAccountBookDetail(String userId, Long accountBookId) {
		UUID userUuid = UUID.fromString(userId);
		return repository
				.findDetailById(userUuid, accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	public AccountBookExchangeRateResponse getAccountBookExchangeRate(
			String userId, Long accountBookId) {
		AccountBookDetailResponse accountBookDetail = getAccountBookDetail(userId, accountBookId);

		CurrencyCode baseCurrencyCode = accountBookDetail.baseCountryCode().getCurrencyCode();
		CurrencyCode localCurrencyCode = accountBookDetail.localCountryCode().getCurrencyCode();
		var budgetCreatedAt = accountBookDetail.budgetCreatedAt();
		if (budgetCreatedAt == null) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_BUDGET_NOT_SET);
		}
		var exchangeRate =
				exchangeRateService.getExchangeRate(
						baseCurrencyCode, localCurrencyCode, budgetCreatedAt);

		return new AccountBookExchangeRateResponse(
				accountBookDetail.baseCountryCode(),
				accountBookDetail.localCountryCode(),
				exchangeRate,
				budgetCreatedAt);
	}

	public List<AccountBookSummaryResponse> getAccountBooks(String userId) {
		UUID userUuid = UUID.fromString(userId);
		Long mainAccountBookId =
				userRepository.findById(userUuid).map(user -> user.getMainBucketId()).orElse(0L);
		return repository.findAllByUserId(userUuid, mainAccountBookId);
	}
}
