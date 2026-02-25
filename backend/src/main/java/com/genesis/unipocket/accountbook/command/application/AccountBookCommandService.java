package com.genesis.unipocket.accountbook.command.application;

import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.port.AccountBookExchangeRateReader;
import com.genesis.unipocket.accountbook.command.application.port.AccountBookUserReader;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookResult;
import com.genesis.unipocket.accountbook.command.application.validator.AccountBookValidator;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateByUserIdArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
	private final AccountBookUserReader accountBookUserReader;
	private final AccountBookValidator validator;
	private final AccountBookExchangeRateReader accountBookExchangeRateReader;
	private final AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;

	@Transactional
	public AccountBookResult create(CreateAccountBookCommand command) {
		var userInfo = accountBookUserReader.getUser(command.userId());
		String uniqueTitle =
				getUniqueTitle(command.userId(), command.userName() + DEFAULT_NAME_SUFFIX);
		int bucketOrder = repository.findMaxBucketOrderByUserId(command.userId()) + 1;

		AccountBookCreateByUserIdArgs args =
				new AccountBookCreateByUserIdArgs(
						command.userId(),
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
		if (!userInfo.hasMainBucket()) {
			accountBookUserReader.updateMainAccountBook(command.userId(), savedEntity.getId());
		}

		return AccountBookResult.of(savedEntity);
	}

	@Transactional
	public AccountBookResult update(UpdateAccountBookCommand command) {

		AccountBookEntity entity =
				findAndVerifyOwnershipWithLock(command.accountBookId(), command.userId());
		CountryCode previousLocalCountryCode = entity.getLocalCountryCode();
		CountryCode previousBaseCountryCode = entity.getBaseCountryCode();

		if (command.budgetPresent()) {
			entity.updateBudget(command.budget());
		}

		if (command.titlePresent()) {
			entity.updateTitle(command.title());
		}

		if (command.startDatePresent() || command.endDatePresent()) {
			entity.changeAccountBookPeriod(
					command.startDatePresent() ? command.startDate() : entity.getStartDate(),
					command.endDatePresent() ? command.endDate() : entity.getEndDate());
		}

		if (command.localCountryCodePresent() || command.baseCountryCodePresent()) {
			entity.updateCountryCodes(
					command.localCountryCodePresent()
							? command.localCountryCode()
							: entity.getLocalCountryCode(),
					command.baseCountryCodePresent()
							? command.baseCountryCode()
							: entity.getBaseCountryCode());
		}

		validator.validate(entity);
		boolean countryChanged =
				previousLocalCountryCode != entity.getLocalCountryCode()
						|| previousBaseCountryCode != entity.getBaseCountryCode();
		if (countryChanged) {
			analysisMonthlyDirtyMarkerService.markDirtyAllMonths(entity.getId());
		}

		return AccountBookResult.of(entity, countryChanged);
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
				accountBookExchangeRateReader.getExchangeRate(
						baseCurrencyCode,
						localCurrencyCode,
						budgetCreatedAt.atOffset(ZoneOffset.UTC));

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

		analysisMonthlyDirtyMarkerService.purgeMonthlyDataByAccountBook(
				entity.getId(), entity.getLocalCountryCode(), entity.getBaseCountryCode());
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

	private AccountBookEntity findAndVerifyOwnershipWithLock(Long accountBookId, UUID userId) {
		AccountBookEntity entity =
				repository
						.findByIdWithLock(accountBookId)
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
