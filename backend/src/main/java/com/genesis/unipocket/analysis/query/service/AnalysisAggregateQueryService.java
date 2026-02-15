package com.genesis.unipocket.analysis.query.service;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.DailyAmountCount;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookMonthlyAggregateRes;
import com.genesis.unipocket.analysis.query.persistence.response.CountryMonthlyAggregateRes;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.AmountFormatUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisAggregateQueryService {

	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisQueryRepository analysisQueryRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public CountryMonthlyAggregateRes getCountryMonthlyAggregate(
			CountryCode countryCode, int year, int month, AnalysisQualityType qualityType) {
		YearMonth yearMonth = parseYearMonth(year, month);
		LocalDate monthStart = yearMonth.atDay(1);
		LocalDate nextMonthStart = yearMonth.plusMonths(1).atDay(1);

		AmountCount totalAmountRow =
				aggregationRepository.aggregateCountryMonthlyFromDaily(
						countryCode,
						monthStart,
						nextMonthStart,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						qualityType);
		AmountCount totalExpenseCountRow =
				aggregationRepository.aggregateCountryMonthlyFromDaily(
						countryCode,
						monthStart,
						nextMonthStart,
						AnalysisMetricType.EXPENSE_COUNT,
						qualityType);

		List<CountryMonthlyAggregateRes.DailyItem> dailyItems =
				aggregationRepository
						.aggregateCountryDailySeries(
								countryCode, monthStart, nextMonthStart, qualityType)
						.stream()
						.map(this::toCountryDailyItem)
						.toList();

		List<CountryMonthlyAggregateRes.CategoryItem> categoryItems =
				aggregationRepository
						.aggregateCountryMonthlyCategoryFromDaily(
								countryCode, monthStart, nextMonthStart, qualityType)
						.stream()
						.filter(row -> row.categoryOrdinal() != null)
						.sorted(
								Comparator.comparing(CategoryAmountCount::totalAmount)
										.reversed()
										.thenComparing(CategoryAmountCount::categoryOrdinal))
						.map(this::toCountryCategoryItem)
						.toList();

		return new CountryMonthlyAggregateRes(
				countryCode,
				year,
				month,
				qualityType,
				AmountFormatUtil.format(totalAmountRow.totalAmount()),
				toLong(totalExpenseCountRow.totalAmount()),
				dailyItems,
				categoryItems);
	}

	public AccountBookMonthlyAggregateRes getAccountBookMonthlyAggregate(
			UUID userId, Long accountBookId, int year, int month, AnalysisQualityType qualityType) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		validateComparableAccountBook(accountBookId);

		YearMonth yearMonth = parseYearMonth(year, month);
		LocalDate monthStart = yearMonth.atDay(1);
		LocalDate nextMonthStart = yearMonth.plusMonths(1).atDay(1);

		AmountCount totalAmountRow =
				aggregationRepository.aggregateAccountMonthlyFromDaily(
						accountBookId,
						monthStart,
						nextMonthStart,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						qualityType);
		AmountCount totalExpenseCountRow =
				aggregationRepository.aggregateAccountMonthlyFromDaily(
						accountBookId,
						monthStart,
						nextMonthStart,
						AnalysisMetricType.EXPENSE_COUNT,
						qualityType);

		List<AccountBookMonthlyAggregateRes.DailyItem> dailyItems =
				aggregationRepository
						.aggregateAccountDailySeries(
								accountBookId, monthStart, nextMonthStart, qualityType)
						.stream()
						.map(this::toAccountDailyItem)
						.toList();

		List<AccountBookMonthlyAggregateRes.CategoryItem> categoryItems =
				aggregationRepository
						.aggregateAccountMonthlyCategoryFromDaily(
								accountBookId, monthStart, nextMonthStart, qualityType)
						.stream()
						.filter(row -> row.categoryOrdinal() != null)
						.sorted(
								Comparator.comparing(CategoryAmountCount::totalAmount)
										.reversed()
										.thenComparing(CategoryAmountCount::categoryOrdinal))
						.map(this::toAccountCategoryItem)
						.toList();

		return new AccountBookMonthlyAggregateRes(
				accountBookId,
				year,
				month,
				qualityType,
				AmountFormatUtil.format(totalAmountRow.totalAmount()),
				toLong(totalExpenseCountRow.totalAmount()),
				dailyItems,
				categoryItems);
	}

	private CountryMonthlyAggregateRes.DailyItem toCountryDailyItem(DailyAmountCount row) {
		return new CountryMonthlyAggregateRes.DailyItem(
				row.targetLocalDate().toString(),
				AmountFormatUtil.format(row.totalAmount()),
				row.expenseCount());
	}

	private CountryMonthlyAggregateRes.CategoryItem toCountryCategoryItem(CategoryAmountCount row) {
		return new CountryMonthlyAggregateRes.CategoryItem(
				row.categoryOrdinal(),
				AmountFormatUtil.format(row.totalAmount()),
				row.expenseCount());
	}

	private AccountBookMonthlyAggregateRes.DailyItem toAccountDailyItem(DailyAmountCount row) {
		return new AccountBookMonthlyAggregateRes.DailyItem(
				row.targetLocalDate().toString(),
				AmountFormatUtil.format(row.totalAmount()),
				row.expenseCount());
	}

	private AccountBookMonthlyAggregateRes.CategoryItem toAccountCategoryItem(
			CategoryAmountCount row) {
		return new AccountBookMonthlyAggregateRes.CategoryItem(
				row.categoryOrdinal(),
				AmountFormatUtil.format(row.totalAmount()),
				row.expenseCount());
	}

	private void validateComparableAccountBook(Long accountBookId) {
		Object[] countryCodes = analysisQueryRepository.getAccountBookCountryCodes(accountBookId);
		CountryCode localCountryCode = (CountryCode) countryCodes[0];
		CountryCode baseCountryCode = (CountryCode) countryCodes[1];
		if (localCountryCode != baseCountryCode) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private YearMonth parseYearMonth(int year, int month) {
		if (month < 1 || month > 12 || year < 1970 || year > 2100) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return YearMonth.of(year, month);
	}

	private long toLong(BigDecimal decimal) {
		return decimal == null ? 0L : decimal.longValue();
	}
}
