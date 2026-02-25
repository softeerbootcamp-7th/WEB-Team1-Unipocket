package com.genesis.unipocket.accountbook.query.service;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookAmountResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookAmountQueryService {

	private static final AnalysisQualityType QUALITY_TYPE = AnalysisQualityType.CLEANED;

	private final AccountBookQueryRepository accountBookQueryRepository;
	private final AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;
	private final AnalysisMonthlyDirtyRepository analysisMonthlyDirtyRepository;
	private final AnalysisBatchAggregationRepository analysisBatchAggregationRepository;
	private final ExchangeRateService exchangeRateService;
	private final ExpenseRepository expenseRepository;

	@Transactional
	public AccountBookAmountResponse getAccountBookAmount(String userId, Long accountBookId) {
		AccountBookDetailResponse accountBook = getAccessibleAccountBook(userId, accountBookId);
		CountryCode localCountryCode = accountBook.localCountryCode();
		CountryCode baseCountryCode = accountBook.baseCountryCode();
		CurrencyCode localCurrencyCode = localCountryCode.getCurrencyCode();
		CurrencyCode baseCurrencyCode = baseCountryCode.getCurrencyCode();

		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);
		LocalDate thisMonthStart = LocalDate.now(zoneId).withDayOfMonth(1);

		List<LocalDate> dirtyMonths = analysisMonthlyDirtyRepository
				.findTargetYearMonthsByCountryCodeAndAccountBookIdAndStatusNot(
						localCountryCode, accountBookId, AnalysisBatchJobStatus.SUCCESS);

		AmountPair total = resolveTotalAmount(accountBookId, localCurrencyCode, zoneId, dirtyMonths);
		AmountPair thisMonth = resolveThisMonthAmount(
				accountBookId, localCurrencyCode, zoneId, thisMonthStart, dirtyMonths);

		Object[] dateRange = expenseRepository.findOccurredAtRangeByAccountBookId(accountBookId);
		OffsetDateTime oldest = dateRange != null && dateRange[0] != null ? (OffsetDateTime) dateRange[0] : null;
		OffsetDateTime newest = dateRange != null && dateRange[1] != null ? (OffsetDateTime) dateRange[1] : null;

		return new AccountBookAmountResponse(
				localCountryCode,
				localCurrencyCode,
				baseCountryCode,
				baseCurrencyCode,
				total.localAmount(),
				total.baseAmount(),
				thisMonth.localAmount(),
				thisMonth.baseAmount(),
				oldest,
				newest);
	}

	private AccountBookDetailResponse getAccessibleAccountBook(String userId, Long accountBookId) {
		UUID userUuid = UUID.fromString(userId);
		return accountBookQueryRepository
				.findDetailById(userUuid, accountBookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
	}

	private AmountPair resolveTotalAmount(
			Long accountBookId,
			CurrencyCode accountBookLocalCurrency,
			ZoneId zoneId,
			List<LocalDate> dirtyMonths) {
		BigDecimal totalLocal = accountMonthlyAggregateRepository
				.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
						accountBookId, AnalysisMetricType.TOTAL_LOCAL_AMOUNT, QUALITY_TYPE);
		BigDecimal totalBase = accountMonthlyAggregateRepository
				.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
						accountBookId, AnalysisMetricType.TOTAL_BASE_AMOUNT, QUALITY_TYPE);

		if (dirtyMonths == null || dirtyMonths.isEmpty()) {
			return new AmountPair(totalLocal, totalBase);
		}

		BigDecimal staleDirtyLocal = accountMonthlyAggregateRepository
				.sumMetricValueByAccountBookIdAndTargetYearMonthInAndMetricTypeAndQualityType(
						accountBookId,
						dirtyMonths,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						QUALITY_TYPE);
		BigDecimal staleDirtyBase = accountMonthlyAggregateRepository
				.sumMetricValueByAccountBookIdAndTargetYearMonthInAndMetricTypeAndQualityType(
						accountBookId,
						dirtyMonths,
						AnalysisMetricType.TOTAL_BASE_AMOUNT,
						QUALITY_TYPE);

		AmountPair dirtyRaw = AmountPair.zero();
		for (LocalDate monthStart : dirtyMonths) {
			dirtyRaw = dirtyRaw.plus(
					resolveMonthlyRawAmount(
							accountBookId, accountBookLocalCurrency, zoneId, monthStart));
		}

		return new AmountPair(
				totalLocal.subtract(staleDirtyLocal).add(dirtyRaw.localAmount()),
				totalBase.subtract(staleDirtyBase).add(dirtyRaw.baseAmount()));
	}

	private AmountPair resolveThisMonthAmount(
			Long accountBookId,
			CurrencyCode accountBookLocalCurrency,
			ZoneId zoneId,
			LocalDate thisMonthStart,
			List<LocalDate> dirtyMonths) {
		boolean dirtyThisMonth = dirtyMonths != null && dirtyMonths.contains(thisMonthStart);
		if (dirtyThisMonth) {
			return resolveMonthlyRawAmount(
					accountBookId, accountBookLocalCurrency, zoneId, thisMonthStart);
		}

		var localAgg = accountMonthlyAggregateRepository
				.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
						accountBookId,
						thisMonthStart,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						QUALITY_TYPE);
		var baseAgg = accountMonthlyAggregateRepository
				.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
						accountBookId,
						thisMonthStart,
						AnalysisMetricType.TOTAL_BASE_AMOUNT,
						QUALITY_TYPE);
		if (localAgg.isPresent() && baseAgg.isPresent()) {
			return new AmountPair(localAgg.get().getMetricValue(), baseAgg.get().getMetricValue());
		}

		return resolveMonthlyRawAmount(
				accountBookId, accountBookLocalCurrency, zoneId, thisMonthStart);
	}

	private AmountPair resolveMonthlyRawAmount(
			Long accountBookId,
			CurrencyCode accountBookLocalCurrency,
			ZoneId zoneId,
			LocalDate monthStart) {
		LocalDate nextMonthStart = monthStart.plusMonths(1);
		LocalDateTime startUtc = toUtc(monthStart, zoneId);
		LocalDateTime endUtc = toUtc(nextMonthStart, zoneId);

		var raw = analysisBatchAggregationRepository.aggregateAccountBookMonthlyRaw(
				accountBookId, startUtc, endUtc);
		BigDecimal correctedLocal = computeCorrectedLocalAmount(
				analysisBatchAggregationRepository.aggregateLocalAmountGroupedByCurrency(
						accountBookId, startUtc, endUtc),
				accountBookLocalCurrency,
				monthStart.atStartOfDay().atOffset(ZoneOffset.UTC));

		return new AmountPair(correctedLocal, raw.totalBaseAmount());
	}

	private BigDecimal computeCorrectedLocalAmount(
			List<AnalysisBatchAggregationRepository.LocalCurrencyGroupRow> groups,
			CurrencyCode targetCurrency,
			OffsetDateTime refDateTime) {
		BigDecimal total = BigDecimal.ZERO;
		for (var group : groups) {
			if (group.localCurrencyCode() == null) {
				continue;
			}
			CurrencyCode from = parseCurrencyCode(group.localCurrencyCode());
			if (from == null) {
				continue;
			}
			BigDecimal amount = group.localAmountSum();
			if (from == targetCurrency) {
				total = total.add(amount);
			} else {
				total = total.add(
						exchangeRateService.convertAmount(
								amount, from, targetCurrency, refDateTime));
			}
		}
		return total;
	}

	private CurrencyCode parseCurrencyCode(String rawCode) {
		if (rawCode == null) {
			return null;
		}
		try {
			int ordinal = Integer.parseInt(rawCode);
			CurrencyCode[] values = CurrencyCode.values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
		} catch (NumberFormatException ignored) {
			try {
				return CurrencyCode.valueOf(rawCode);
			} catch (IllegalArgumentException ignored2) {
				return null;
			}
		}
	}

	private LocalDateTime toUtc(LocalDate localDate, ZoneId zoneId) {
		return localDate.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	private record AmountPair(BigDecimal localAmount, BigDecimal baseAmount) {
		private static AmountPair zero() {
			return new AmountPair(BigDecimal.ZERO, BigDecimal.ZERO);
		}

		private AmountPair plus(AmountPair other) {
			return new AmountPair(
					localAmount.add(other.localAmount()), baseAmount.add(other.baseAmount()));
		}
	}
}
