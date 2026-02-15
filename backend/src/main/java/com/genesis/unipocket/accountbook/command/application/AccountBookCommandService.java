package com.genesis.unipocket.accountbook.command.application;

import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookResult;
import com.genesis.unipocket.accountbook.command.application.validator.AccountBookValidator;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookCommandService {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)$");
	private static final String DEFAULT_NAME_SUFFIX = "의 가계부";
	private static final CountryCode DEFAULT_BASE_COUNTRY_CODE = CountryCode.KR;

	private final AccountBookCommandRepository repository;
	private final UserCommandRepository userRepository;
	private final AccountBookValidator validator;
	private final ExchangeRateService exchangeRateService;

	@Transactional
	public AccountBookResult create(CreateAccountBookCommand command) {
		UserEntity user =
				userRepository
						.findById(command.userId())
						.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		String uniqueTitle =
				getUniqueTitle(command.userId(), command.userName() + DEFAULT_NAME_SUFFIX);
		boolean isFirstAccountBook = repository.countByUser_Id(command.userId()) == 0;
		int bucketOrder =
				isFirstAccountBook
						? 0
						: repository.findMaxBucketOrderByUserId(command.userId()) + 1;

		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						user,
						uniqueTitle,
						command.localCountryCode(),
						DEFAULT_BASE_COUNTRY_CODE,
						bucketOrder,
						null,
						command.startDate(),
						command.endDate());

		AccountBookEntity newEntity = AccountBookEntity.create(args);
		validator.validate(newEntity);
		AccountBookEntity savedEntity = repository.save(newEntity);
		if (!user.hasMainBucket()) {
			user.updateMainBucketId(savedEntity.getId());
		}

		return AccountBookResult.of(savedEntity);
	}

	@Transactional
	public AccountBookResult update(UpdateAccountBookCommand command) {

		AccountBookEntity entity =
				findAndVerifyOwnership(command.accountBookId(), command.userId());

		entity.updateBudget(command.budget());
		entity.updateTitle(command.title());
		entity.changeAccountBookPeriod(command.startDate(), command.endDate());
		entity.updateCountryCodes(command.localCountryCode(), command.baseCountryCode());

		validator.validate(entity);

		return AccountBookResult.of(entity);
	}

	@Transactional
	public AccountBookBudgetUpdateResult updateBudget(
			Long accountBookId, UUID userId, BigDecimal budget) {
		AccountBookEntity entity = findAndVerifyOwnership(accountBookId, userId);
		entity.updateBudget(budget);
		validator.validate(entity);

		CurrencyCode baseCurrencyCode = entity.getBaseCountryCode().getCurrencyCode();
		CurrencyCode localCurrencyCode = entity.getLocalCountryCode().getCurrencyCode();
		LocalDateTime budgetCreatedAt = entity.getBudgetCreatedAt();
		BigDecimal exchangeRate =
				exchangeRateService.getExchangeRate(
						baseCurrencyCode, localCurrencyCode, budgetCreatedAt);

		return new AccountBookBudgetUpdateResult(
				entity.getId(),
				entity.getBaseCountryCode(),
				entity.getLocalCountryCode(),
				entity.getBudget(),
				budgetCreatedAt,
				exchangeRate);
	}

	@Transactional
	public void delete(DeleteAccountBookCommand command) {
		AccountBookEntity entity =
				findAndVerifyOwnership(command.accountBookId(), command.userId());

		repository.delete(entity);
	}

	private AccountBookEntity findAndVerifyOwnership(Long accountBookId, UUID userId) {
		AccountBookEntity entity =
				repository
						.findById(accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
		if (!entity.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
		}
		return entity;
	}

	private String getUniqueTitle(UUID userId, String baseTitle) {
		List<String> existingNames = repository.findNamesStartingWith(userId, baseTitle);

		if (existingNames.isEmpty()) {
			return baseTitle + "1";
		}

		int maxNum = 0;

		for (String name : existingNames) {
			if (name.equals(baseTitle)) continue;

			Matcher matcher = NUMBER_PATTERN.matcher(name);
			if (matcher.find()) {
				try {
					int num = Integer.parseInt(matcher.group(1));
					maxNum = Math.max(maxNum, num);
				} catch (NumberFormatException e) {
					// 숫자가 너무 커서 파싱 불가능한 경우는 무시
				}
			}
		}

		return baseTitle + (maxNum + 1);
	}
}
