package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.accountbook.common.validation.AccountBookPeriodValidator;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseConversionAmount;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final FileRepository fileRepository;
	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;
	private final TemporaryExpenseValidator temporaryExpenseValidator;
	private final AccountBookPeriodValidator accountBookPeriodValidator;
	private final AccountBookCommandRepository accountBookCommandRepository;
	private final UserCardCommandRepository userCardCommandRepository;

	@Transactional
	public ConfirmStartResult startConfirmAsync(Long accountBookId, Long tempExpenseMetaId) {
		temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		List<TemporaryExpense> expenses =
				tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId);
		if (expenses.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}

		AccountBookRateInfo rateInfo = accountBookRateInfoProvider.getRateInfo(accountBookId);
		CurrencyCode defaultBaseCurrencyCode = rateInfo.baseCurrencyCode();
		CurrencyCode defaultLocalCurrencyCode = rateInfo.localCurrencyCode();
		ZoneId localZoneId = CountryCodeTimezoneMapper.getZoneId(rateInfo.localCountryCode());
		AccountBookEntity accountBook = getAccountBook(accountBookId);
		CardMatchContext cardMatchContext = buildCardMatchContext(accountBook);

		List<TempExpenseConvertValidationException.Violation> violations = new ArrayList<>();
		for (TemporaryExpense expense : expenses) {
			List<String> missing =
					temporaryExpenseValidator.findMissingOrInvalidFields(
							expense, defaultBaseCurrencyCode);
			if (!missing.isEmpty()) {
				violations.add(
						new TempExpenseConvertValidationException.Violation(
								expense.getTempExpenseId(), missing));
			}
		}
		if (!violations.isEmpty()) {
			throw new TempExpenseConvertValidationException(violations);
		}

		for (TemporaryExpense expense : expenses) {
			convertOne(
					accountBookId,
					expense,
					accountBook,
					defaultBaseCurrencyCode,
					defaultLocalCurrencyCode,
					localZoneId,
					cardMatchContext);
		}
		return new ConfirmStartResult(null, expenses.size());
	}

	private void convertOne(
			Long accountBookId,
			TemporaryExpense temp,
			AccountBookEntity accountBook,
			CurrencyCode defaultBaseCurrencyCode,
			CurrencyCode defaultLocalCurrencyCode,
			ZoneId localZoneId,
			CardMatchContext cardMatchContext) {
		temporaryExpenseValidator.validateConvertible(temp, defaultBaseCurrencyCode);

		File file =
				temp.getFileId() != null
						? fileRepository.findById(temp.getFileId()).orElse(null)
						: null;

		OffsetDateTime occurredAtUtc =
				temp.getOccurredAt()
						.atZone(localZoneId)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toOffsetDateTime();
		accountBookPeriodValidator.validate(
				accountBook.getLocalCountryCode(),
				accountBook.getStartDate(),
				accountBook.getEndDate(),
				occurredAtUtc);

		var amountInfo = temp.getAmountInfoOrEmpty();
		TempExpenseConversionAmount conversionAmount =
				amountInfo.resolveForConversion(
						defaultLocalCurrencyCode,
						defaultBaseCurrencyCode,
						occurredAtUtc,
						exchangeRateService);
		Long matchedUserCardId = resolveUserCardId(cardMatchContext, temp.getCardLastFourDigits());

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						accountBookId,
						temp.getMerchantName(),
						temp.getCategory(),
						matchedUserCardId,
						occurredAtUtc,
						amountInfo.getLocalCurrencyAmount(),
						conversionAmount.localCurrencyCode(),
						conversionAmount.baseCurrencyAmount(),
						conversionAmount.baseCurrencyCode(),
						conversionAmount.calculatedBaseCurrencyAmount(),
						conversionAmount.baseCurrencyCode(),
						temp.getMemo(),
						null,
						conversionAmount.exchangeRate());

		ExpenseEntity expense =
				ExpenseEntity.convertedFromTemporary(
						args,
						resolveExpenseSource(file != null ? file.getFileType() : null),
						file != null ? file.getS3Key() : null,
						temp.getApprovalNumber(),
						temp.getCardLastFourDigits());
		expenseRepository.save(expense);
		tempExpenseRepository.delete(temp);
	}

	private CardMatchContext buildCardMatchContext(AccountBookEntity accountBook) {
		List<UserCardEntity> userCards =
				userCardCommandRepository.findAllByUser_Id(accountBook.getUser().getId());
		Map<String, Long> cardIdByLastFourDigits =
				userCards.stream()
						.filter(
								card ->
										card.getCardNumber() != null
												&& !card.getCardNumber().isBlank())
						.collect(
								Collectors.toMap(
										UserCardEntity::getCardNumber,
										UserCardEntity::getUserCardId,
										(existing, replacement) -> existing));
		Set<String> duplicateLastFourDigits =
				userCards.stream()
						.map(UserCardEntity::getCardNumber)
						.filter(number -> number != null && !number.isBlank())
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
						.entrySet()
						.stream()
						.filter(entry -> entry.getValue() > 1)
						.map(Map.Entry::getKey)
						.collect(Collectors.toCollection(HashSet::new));
		return new CardMatchContext(cardIdByLastFourDigits, duplicateLastFourDigits);
	}

	private Long resolveUserCardId(CardMatchContext cardMatchContext, String cardLastFourDigits) {
		if (cardLastFourDigits == null || cardLastFourDigits.isBlank()) {
			// 카드 4자리가 없으면 현금/기타 결제로 간주 (카드 미연동)
			return null;
		}
		if (cardMatchContext.duplicateLastFourDigits().contains(cardLastFourDigits)) {
			log.warn(
					"Skipped temp-expense card auto mapping due to duplicate last four digits: {}",
					cardLastFourDigits);
			return null;
		}
		return cardMatchContext.cardIdByLastFourDigits().get(cardLastFourDigits);
	}

	private AccountBookEntity getAccountBook(Long accountBookId) {
		return accountBookCommandRepository
				.findById(accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	private record CardMatchContext(
			Map<String, Long> cardIdByLastFourDigits, Set<String> duplicateLastFourDigits) {}

	private ExpenseSource resolveExpenseSource(FileType fileType) {
		if (fileType == null) {
			return ExpenseSource.MANUAL;
		}
		return switch (fileType) {
			case IMAGE -> ExpenseSource.IMAGE_RECEIPT;
			case CSV -> ExpenseSource.CSV;
			case EXCEL -> ExpenseSource.EXCEL;
		};
	}
}
