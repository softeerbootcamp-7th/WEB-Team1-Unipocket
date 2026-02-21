package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.exception.TemporaryExpenseGeminiRateLimitException;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class TemporaryExpenseParseFileService {

	private final TemporaryExpenseFieldParser fieldParser;
	private final TemporaryExpenseParseClient temporaryExpenseParseClient;
	private final ExchangeRateProvider exchangeRateProvider;
	private final TemporaryExpensePersistenceService temporaryExpensePersistenceService;

	TemporaryExpenseParseFileService(
			TemporaryExpenseFieldParser fieldParser,
			TemporaryExpenseParseClient temporaryExpenseParseClient,
			ExchangeRateProvider exchangeRateProvider,
			TemporaryExpensePersistenceService temporaryExpensePersistenceService) {
		this.fieldParser = fieldParser;
		this.temporaryExpenseParseClient = temporaryExpenseParseClient;
		this.exchangeRateProvider = exchangeRateProvider;
		this.temporaryExpensePersistenceService = temporaryExpensePersistenceService;
	}

	void parseFileAndSaveTempExpenses(
			File file, TempExpenseMeta meta, AccountBookRateContext rateContext) {
		var geminiResponse = temporaryExpenseParseClient.parseFile(file);
		if (!geminiResponse.success()) {
			if (geminiResponse.isRateLimited()) {
				log.error("Gemini rate limited. fileId={}", file.getFileId());
				throw new TemporaryExpenseGeminiRateLimitException();
			}
			log.error(
					"Gemini parsing failed. fileId={}, reason={}",
					file.getFileId(),
					geminiResponse.errorMessage());
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
		}

		List<NormalizedParsedExpenseItem> normalizedItems =
				geminiResponse.items().stream()
						.map(
								item ->
										normalizeParsedItem(
												item, rateContext.defaultLocalCurrencyCode()))
						.toList();
		Map<ExchangeRateLookupCommand, BigDecimal> exchangeRateMap =
				buildExchangeRateMap(normalizedItems, rateContext.baseCurrencyCode());

		temporaryExpensePersistenceService.saveParsedExpenses(
				file, meta, normalizedItems, rateContext, exchangeRateMap);
	}

	private NormalizedParsedExpenseItem normalizeParsedItem(
			GeminiService.ParsedExpenseItem item, CurrencyCode defaultLocalCurrencyCode) {
		return new NormalizedParsedExpenseItem(
				item.merchantName(),
				fieldParser.parseCategory(item.category()),
				fieldParser.parseCurrencyCode(item.localCurrency(), defaultLocalCurrencyCode),
				item.localAmount(),
				fieldParser.parseCurrencyCode(item.baseCurrency(), null),
				item.baseAmount(),
				item.memo(),
				item.occurredAt(),
				item.cardLastFourDigits(),
				item.approvalNumber());
	}

	private Map<ExchangeRateLookupCommand, BigDecimal> buildExchangeRateMap(
			List<NormalizedParsedExpenseItem> items, CurrencyCode baseCurrencyCode) {
		Set<ExchangeRateLookupCommand> lookupKeys =
				items.stream()
						.filter(
								item ->
										item.localAmount() != null
												&& item.occurredAt() != null
												&& !(item.baseAmount() != null
														&& item.baseCurrencyCode() != null
														&& item.baseCurrencyCode()
																== baseCurrencyCode))
						.map(
								item ->
										new ExchangeRateLookupCommand(
												item.localCurrencyCode(),
												baseCurrencyCode,
												item.occurredAt().toLocalDate()))
						.collect(Collectors.toSet());

		Map<ExchangeRateLookupCommand, BigDecimal> rateMap = new HashMap<>();
		for (ExchangeRateLookupCommand key : lookupKeys) {
			if (key.fromCurrencyCode() == key.toCurrencyCode()) {
				rateMap.put(key, BigDecimal.ONE);
				continue;
			}
			BigDecimal rate =
					exchangeRateProvider.getExchangeRate(
							key.fromCurrencyCode(),
							key.toCurrencyCode(),
							key.date().atStartOfDay().atOffset(ZoneOffset.UTC));
			rateMap.put(key, rate);
		}
		return rateMap;
	}
}
