package com.genesis.unipocket.widget.query.service;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.global.util.AmountFormatUtil;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import com.genesis.unipocket.global.util.PercentUtil;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import com.genesis.unipocket.widget.query.service.PeriodRangeUtil.PeriodSlot;
import com.genesis.unipocket.widget.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.widget.query.persistence.WidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.response.BudgetWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CategoryWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CategoryWidgetResponse.CategoryItem;
import com.genesis.unipocket.widget.query.persistence.response.ComparisonWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CurrencyWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CurrencyWidgetResponse.CurrencyItem;
import com.genesis.unipocket.widget.query.persistence.response.PaymentWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.PaymentWidgetResponse.PaymentItem;
import com.genesis.unipocket.widget.query.persistence.response.PeriodWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.PeriodWidgetResponse.PeriodItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetQueryService {

	private static final String ETC_LABEL = "기타";
	private static final int CATEGORY_TOP_COUNT = 6;
	private static final int PAYMENT_TOP_COUNT = 4;
	private static final int CURRENCY_TOP_COUNT = 4;

	private final WidgetQueryRepository widgetQueryRepository;
	private final UserAccountBookValidator userAccountBookValidator;

	public Object getWidget(
			UUID userId,
			Long accountBookId,
			Long travelId,
			WidgetType widgetType,
			CurrencyType currencyType,
			Period period) {

		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);

		CurrencyType resolvedCurrencyType = currencyType != null ? currencyType : CurrencyType.BASE;
		Period resolvedPeriod = period != null ? period : Period.ALL;

		CountryCode baseCountryCode =
				widgetQueryRepository.getAccountBookCountryCode(accountBookId, CurrencyType.BASE);
		CountryCode localCountryCode =
				widgetQueryRepository.getAccountBookCountryCode(accountBookId, CurrencyType.LOCAL);

		// TODO: 한 국가에 타임존이 여러개인 경우가 있으므로 이 부분에 대한 논의 필요
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);

		LocalDateTime periodStart = null;
		LocalDateTime periodEnd = null;
		LocalDateTime[] range = PeriodRangeUtil.getCurrentPeriodRange(resolvedPeriod, zoneId);
		if (range != null) {
			periodStart = range[0];
			periodEnd = range[1];
		}

		WidgetQueryContext context =
				new WidgetQueryContext(
						accountBookId,
						travelId,
						resolvedCurrencyType,
						resolvedPeriod,
						zoneId,
						periodStart,
						periodEnd,
						baseCountryCode,
						localCountryCode);

		return switch (widgetType) {
			case BUDGET -> getBudgetWidget(context);
			case PERIOD -> getPeriodWidget(context);
			case CATEGORY -> getCategoryWidget(context);
			case COMPARISON -> getComparisonWidget(context);
			case PAYMENT -> getPaymentWidget(context);
			case CURRENCY -> getCurrencyWidget(context);
		};
	}

	private BudgetWidgetResponse getBudgetWidget(WidgetQueryContext context) {
		return BudgetWidgetResponse.builder()
				.budget(
						AmountFormatUtil.format(
								widgetQueryRepository.getBudget(context.accountBookId())))
				.baseCountryCode(context.baseCountryCode())
				.localCountryCode(context.localCountryCode())
				.baseSpentAmount(
						AmountFormatUtil.format(
								widgetQueryRepository.getTotalSpentByAccountBookId(
										context.accountBookId(),
										context.travelId(),
										CurrencyType.BASE)))
				.localSpentAmount(
						AmountFormatUtil.format(
								widgetQueryRepository.getTotalSpentByAccountBookId(
										context.accountBookId(),
										context.travelId(),
										CurrencyType.LOCAL)))
				.build();
	}

	private PeriodWidgetResponse getPeriodWidget(WidgetQueryContext context) {
		CountryCode countryCode =
				context.currencyType() == CurrencyType.LOCAL
						? context.localCountryCode()
						: context.baseCountryCode();

		List<PeriodSlot> slots =
				PeriodRangeUtil.getRecentPeriodSlots(context.period(), context.zoneId());

		List<PeriodItem> items =
				slots.stream()
						.map(
								slot -> {
									BigDecimal amount =
											widgetQueryRepository.findSpentInRange(
													context.accountBookId(),
													context.travelId(),
													context.currencyType(),
													slot.start(),
													slot.end());
									return new PeriodItem(
											slot.label(), AmountFormatUtil.format(amount));
								})
						.toList();

		return new PeriodWidgetResponse(countryCode, items.size(), items);
	}

	private CategoryWidgetResponse getCategoryWidget(WidgetQueryContext context) {
		CountryCode countryCode =
				context.currencyType() == CurrencyType.LOCAL
						? context.localCountryCode()
						: context.baseCountryCode();

		List<Object[]> rows =
				widgetQueryRepository.findCategorySpentByAccountBookId(
						context.accountBookId(),
						context.travelId(),
						context.currencyType(),
						context.periodStart(),
						context.periodEnd());

		BigDecimal totalAmount =
				rows.stream().map(r -> toBigDecimal(r[1])).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal unclassifiedAmount =
				rows.stream()
						.filter(r -> r[0] == Category.UNCLASSIFIED)
						.map(r -> toBigDecimal(r[1]))
						.findFirst()
						.orElse(BigDecimal.ZERO);

		List<Object[]> classifiedRows =
				rows.stream().filter(r -> r[0] != Category.UNCLASSIFIED).toList();

		List<Object[]> topRows = classifiedRows.stream().limit(CATEGORY_TOP_COUNT).toList();

		BigDecimal overflowSum =
				classifiedRows.stream()
						.skip(CATEGORY_TOP_COUNT)
						.map(r -> toBigDecimal(r[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		unclassifiedAmount = unclassifiedAmount.add(overflowSum);

		List<Object[]> finalRows = new ArrayList<>(topRows.size() + 1);
		finalRows.addAll(topRows);
		if (unclassifiedAmount.compareTo(BigDecimal.ZERO) > 0) {
			finalRows.add(new Object[] {Category.UNCLASSIFIED, unclassifiedAmount});
		}

		List<BigDecimal> amounts = finalRows.stream().map(r -> toBigDecimal(r[1])).toList();
		List<Integer> percents = PercentUtil.distributePercents(amounts, totalAmount);

		List<CategoryItem> items = new ArrayList<>(finalRows.size());
		for (int i = 0; i < finalRows.size(); i++) {
			Category category = (Category) finalRows.get(i)[0];
			items.add(
					new CategoryItem(
							category.getName(),
							AmountFormatUtil.format(toBigDecimal(finalRows.get(i)[1])),
							percents.get(i)));
		}

		return new CategoryWidgetResponse(AmountFormatUtil.format(totalAmount), countryCode, items);
	}

	private ComparisonWidgetResponse getComparisonWidget(WidgetQueryContext context) {
		ZonedDateTime now = ZonedDateTime.now(context.zoneId());
		ZonedDateTime monthStart =
				now.with(TemporalAdjusters.firstDayOfMonth())
						.toLocalDate()
						.atStartOfDay(context.zoneId());
		ZonedDateTime monthEnd = monthStart.plusMonths(1);

		LocalDateTime utcMonthStart =
				monthStart.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
		LocalDateTime utcMonthEnd =
				monthEnd.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

		CountryCode countryCode =
				context.currencyType() == CurrencyType.LOCAL
						? context.localCountryCode()
						: context.baseCountryCode();

		BigDecimal mySpentAmount =
				widgetQueryRepository.findMonthlyTotalByAccountBookId(
						context.accountBookId(),
						context.travelId(),
						context.currencyType(),
						utcMonthStart,
						utcMonthEnd);

		BigDecimal averageSpentAmount =
				widgetQueryRepository.findAverageMonthlySpentByCountryCode(
						context.localCountryCode(),
						context.currencyType(),
						utcMonthStart,
						utcMonthEnd);

		BigDecimal spentAmountDiff = mySpentAmount.subtract(averageSpentAmount).abs();

		return new ComparisonWidgetResponse(
				countryCode,
				now.getMonthValue(),
				AmountFormatUtil.format(mySpentAmount),
				AmountFormatUtil.format(averageSpentAmount),
				AmountFormatUtil.format(spentAmountDiff));
	}

	private PaymentWidgetResponse getPaymentWidget(WidgetQueryContext context) {
		List<Object[]> rows =
				widgetQueryRepository.findPaymentMethodSpentByAccountBookId(
						context.accountBookId(),
						context.travelId(),
						context.currencyType(),
						context.periodStart(),
						context.periodEnd());

		BigDecimal totalAmount =
				rows.stream()
						.map(row -> toBigDecimal(row[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		int totalDistinctCount = rows.size();

		List<Object[]> namedRows = rows.stream().filter(r -> r[0] != null).toList();

		List<Object[]> nullRows = rows.stream().filter(r -> r[0] == null).toList();

		BigDecimal nullAmount =
				nullRows.stream()
						.map(r -> toBigDecimal(r[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<Object[]> topRows = namedRows.stream().limit(PAYMENT_TOP_COUNT).toList();

		BigDecimal overflowAmount =
				namedRows.stream()
						.skip(PAYMENT_TOP_COUNT)
						.map(r -> toBigDecimal(r[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal etcAmount = nullAmount.add(overflowAmount);

		List<String> names = new ArrayList<>();
		List<BigDecimal> amounts = new ArrayList<>();
		for (Object[] row : topRows) {
			names.add((String) row[0]);
			amounts.add(toBigDecimal(row[1]));
		}
		if (etcAmount.compareTo(BigDecimal.ZERO) > 0) {
			names.add(ETC_LABEL);
			amounts.add(etcAmount);
		}

		List<Integer> percents = PercentUtil.distributePercents(amounts, totalAmount);

		List<PaymentItem> items = new ArrayList<>(names.size());
		for (int i = 0; i < names.size(); i++) {
			items.add(new PaymentItem(names.get(i), percents.get(i)));
		}

		return new PaymentWidgetResponse(totalDistinctCount, items);
	}

	private CurrencyWidgetResponse getCurrencyWidget(WidgetQueryContext context) {
		List<Object[]> rows =
				widgetQueryRepository.findCurrencySpentByAccountBookId(
						context.accountBookId(),
						context.travelId(),
						context.currencyType(),
						context.periodStart(),
						context.periodEnd());

		BigDecimal totalAmount =
				rows.stream()
						.map(row -> toBigDecimal(row[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		int totalDistinctCount = rows.size();

		List<Object[]> topRows = rows.stream().limit(CURRENCY_TOP_COUNT).toList();

		BigDecimal overflowAmount =
				rows.stream()
						.skip(CURRENCY_TOP_COUNT)
						.map(r -> toBigDecimal(r[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<CurrencyCode> codes = new ArrayList<>();
		List<BigDecimal> amounts = new ArrayList<>();
		for (Object[] row : topRows) {
			CurrencyCode code = (CurrencyCode) row[0];
			codes.add(code);
			amounts.add(toBigDecimal(row[1]));
		}
		if (overflowAmount.compareTo(BigDecimal.ZERO) > 0) {
			codes.add(null);
			amounts.add(overflowAmount);
		}

		List<Integer> percents = PercentUtil.distributePercents(amounts, totalAmount);

		List<CurrencyItem> items = new ArrayList<>(codes.size());
		for (int i = 0; i < codes.size(); i++) {
			items.add(new CurrencyItem(codes.get(i), percents.get(i)));
		}

		return new CurrencyWidgetResponse(totalDistinctCount, items);
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal bd) {
			return bd.setScale(2, RoundingMode.DOWN);
		}
		return new BigDecimal(value.toString()).setScale(2, RoundingMode.DOWN);
	}
}
