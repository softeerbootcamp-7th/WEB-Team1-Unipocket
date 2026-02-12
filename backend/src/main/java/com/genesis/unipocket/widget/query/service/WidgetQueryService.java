package com.genesis.unipocket.widget.query.service;

import static com.genesis.unipocket.widget.common.enums.Period.WEEKLY;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import com.genesis.unipocket.widget.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.widget.query.persistence.WidgetQueryRepository;
import com.genesis.unipocket.widget.query.persistence.response.BudgetWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CategoryWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CategoryWidgetResponse.CategoryItem;
import com.genesis.unipocket.widget.query.persistence.response.CurrencyWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.CurrencyWidgetResponse.CurrencyItem;
import com.genesis.unipocket.widget.query.persistence.response.MonthlyWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.PaymentWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.PaymentWidgetResponse.PaymentItem;
import com.genesis.unipocket.widget.query.persistence.response.PeriodWidgetResponse;
import com.genesis.unipocket.widget.query.persistence.response.PeriodWidgetResponse.PeriodItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetQueryService {

	private final WidgetQueryRepository widgetQueryRepository;
	private final UserAccountBookValidator userAccountBookValidator;

	public BudgetWidgetResponse getBudgetWidget(UUID userId, Long accountBookId) {
		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);
		return BudgetWidgetResponse.builder()
				.budget(widgetQueryRepository.getBudget(accountBookId))
				.baseCountryCode(
						widgetQueryRepository.getAccountBookCountryCode(
								accountBookId, CurrencyType.BASE))
				.localCountryCode(
						widgetQueryRepository.getAccountBookCountryCode(
								accountBookId, CurrencyType.LOCAL))
				.baseSpentAmount(
						widgetQueryRepository.getTotalSpentByAccountBookId(
								accountBookId, CurrencyType.BASE))
				.localSpentAmount(
						widgetQueryRepository.getTotalSpentByAccountBookId(
								accountBookId, CurrencyType.LOCAL))
				.build();
	}

	public CategoryWidgetResponse getCategoryWidget(UUID userId, Long accountBookId) {
		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);

		CountryCode countryCode =
				widgetQueryRepository.getAccountBookCountryCode(accountBookId, CurrencyType.BASE);

		List<Object[]> rows = widgetQueryRepository.findCategorySpentByAccountBookId(accountBookId);

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

		List<Object[]> topRows = classifiedRows.stream().limit(6).toList();

		BigDecimal overflowSum =
				classifiedRows.stream()
						.skip(6)
						.map(r -> toBigDecimal(r[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		unclassifiedAmount = unclassifiedAmount.add(overflowSum);

		List<Object[]> finalRows = new java.util.ArrayList<>(topRows.size() + 1);
		finalRows.addAll(topRows);

		if (unclassifiedAmount.compareTo(BigDecimal.ZERO) > 0) {
			finalRows.add(new Object[] {Category.UNCLASSIFIED, unclassifiedAmount});
		}

		List<CategoryItem> items = buildCategoryItemsWithPercentFix(finalRows, totalAmount);

		return new CategoryWidgetResponse(totalAmount, countryCode, items);
	}

	public CurrencyWidgetResponse getCurrencyWidget(UUID userId, Long accountBookId) {
		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);

		List<Object[]> rows = widgetQueryRepository.findCurrencySpentByAccountBookId(accountBookId);

		BigDecimal totalAmount =
				rows.stream()
						.map(row -> toBigDecimal(row[1]))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<CurrencyItem> items =
				rows.stream()
						.map(
								row -> {
									CurrencyCode code = (CurrencyCode) row[0];
									BigDecimal amount = toBigDecimal(row[1]);
									int percent = calculatePercent(amount, totalAmount);
									return new CurrencyItem(code, percent);
								})
						.toList();

		return new CurrencyWidgetResponse(items.size(), items);
	}

	private int calculatePercent(BigDecimal amount, BigDecimal total) {
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}
		return amount.multiply(BigDecimal.valueOf(100))
				.divide(total, 0, RoundingMode.HALF_UP)
				.intValue();
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal bd) {
			return bd.setScale(2, RoundingMode.DOWN);
		}
		return new BigDecimal(value.toString()).setScale(2, RoundingMode.DOWN);
	}

	// 퍼센테이지의 합을 100으로 맞춰주는 메서드
	private List<CategoryItem> buildCategoryItemsWithPercentFix(
			List<Object[]> rows, BigDecimal totalAmount) {
		if (rows.isEmpty()) {
			return List.of();
		}
		if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
			return rows.stream()
					.map(r -> new CategoryItem((Category) r[0], toBigDecimal(r[1]), 0))
					.toList();
		}

		List<CategoryItem> items = new java.util.ArrayList<>(rows.size());
		int percentSum = 0;

		for (int i = 0; i < rows.size(); i++) {
			Category category = (Category) rows.get(i)[0];
			BigDecimal amount = toBigDecimal(rows.get(i)[1]);

			int percent;
			if (i < rows.size() - 1) {
				percent = calculatePercent(amount, totalAmount);
				percentSum += percent;
			} else {
				percent = Math.max(0, 100 - percentSum);
			}
			items.add(new CategoryItem(category, amount, percent));
		}
		return items;
	}
}
