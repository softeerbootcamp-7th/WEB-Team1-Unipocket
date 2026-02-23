package com.genesis.unipocket.expense.query.persistence.repository;

import com.genesis.unipocket.expense.command.persistence.entity.QExpenseEntity;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.command.persistence.entity.QTravel;
import com.genesis.unipocket.user.command.persistence.entity.QUserCardEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.EnumExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExpenseQueryDslRepository {

	private final JPAQueryFactory queryFactory;

	public Optional<ExpenseOneShotRow> findExpenseOneShot(Long accountBookId, Long expenseId) {
		QExpenseEntity expense = QExpenseEntity.expenseEntity;
		QTravel travel = QTravel.travel;
		QUserCardEntity userCard = QUserCardEntity.userCardEntity;

		ExpenseOneShotRow row =
				queryFactory
						.select(expenseOneShotProjection(expense, travel, userCard))
						.from(expense)
						.leftJoin(travel)
						.on(
								travel.id.eq(expense.travelId),
								travel.accountBookId.eq(expense.accountBookId))
						.leftJoin(userCard)
						.on(userCard.userCardId.eq(expense.userCardId))
						.where(
								expense.accountBookId.eq(accountBookId),
								expense.expenseId.eq(expenseId))
						.fetchFirst();

		return Optional.ofNullable(row);
	}

	public Page<ExpenseOneShotRow> findExpensesOneShot(
			Long accountBookId, ExpenseSearchFilter filter, Pageable pageable) {
		QExpenseEntity expense = QExpenseEntity.expenseEntity;
		QTravel travel = QTravel.travel;
		QUserCardEntity userCard = QUserCardEntity.userCardEntity;

		BooleanBuilder predicate = buildPredicate(accountBookId, filter, expense, userCard);
		OrderSpecifier<?>[] orderSpecifiers = toOrderSpecifiers(pageable.getSort(), expense);

		List<ExpenseOneShotRow> content =
				queryFactory
						.select(expenseOneShotProjection(expense, travel, userCard))
						.from(expense)
						.leftJoin(travel)
						.on(
								travel.id.eq(expense.travelId),
								travel.accountBookId.eq(expense.accountBookId))
						.leftJoin(userCard)
						.on(userCard.userCardId.eq(expense.userCardId))
						.where(predicate)
						.orderBy(orderSpecifiers)
						.offset(pageable.getOffset())
						.limit(pageable.getPageSize())
						.fetch();

		Long total =
				queryFactory
						.select(expense.expenseId.count())
						.from(expense)
						.leftJoin(userCard)
						.on(userCard.userCardId.eq(expense.userCardId))
						.where(predicate)
						.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0L : total);
	}

	public List<String> findMerchantNameSuggestions(Long accountBookId, String prefix, int limit) {
		QExpenseEntity expense = QExpenseEntity.expenseEntity;

		return queryFactory
				.select(expense.merchant.displayMerchantName)
				.from(expense)
				.where(
						expense.accountBookId.eq(accountBookId),
						expense.merchant.displayMerchantName.startsWith(prefix))
				.groupBy(expense.merchant.displayMerchantName)
				.orderBy(expense.updatedAt.max().desc())
				.limit(limit)
				.fetch();
	}

	private BooleanBuilder buildPredicate(
			Long accountBookId,
			ExpenseSearchFilter filter,
			QExpenseEntity expense,
			QUserCardEntity userCard) {
		BooleanBuilder predicate = new BooleanBuilder(expense.accountBookId.eq(accountBookId));

		if (filter == null) {
			return predicate;
		}

		if (filter.startDate() != null) {
			predicate.and(expense.occurredAt.goe(filter.startDate()));
		}
		if (filter.endDate() != null) {
			predicate.and(expense.occurredAt.lt(filter.endDate()));
		}
		if (filter.travelId() != null) {
			predicate.and(expense.travelId.eq(filter.travelId()));
		}
		if (hasValues(filter.category())) {
			predicate.and(expense.category.in(filter.category()));
		}
		if (hasValues(filter.cardFourDigits())) {
			predicate.and(userCard.cardNumber.in(filter.cardFourDigits()));
		}
		if (hasValues(filter.merchantName())) {
			BooleanBuilder merchantOr = new BooleanBuilder();
			StringPath merchantNamePath = expense.merchant.displayMerchantName;
			for (String keyword : filter.merchantName()) {
				if (keyword == null || keyword.isBlank()) {
					continue;
				}
				merchantOr.or(merchantNamePath.contains(keyword.trim()));
			}
			if (merchantOr.hasValue()) {
				predicate.and(merchantOr);
			}
		}

		return predicate;
	}

	private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort, QExpenseEntity expense) {
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

		if (sort != null && sort.isSorted()) {
			for (Sort.Order order : sort) {
				boolean asc = order.isAscending();
				switch (order.getProperty()) {
					case "occurredAt" ->
							orderSpecifiers.add(
									asc ? expense.occurredAt.asc() : expense.occurredAt.desc());
					case "baseCurrencyAmount" ->
							orderSpecifiers.add(
									asc
											? displayBaseAmountExpression(expense).asc()
											: displayBaseAmountExpression(expense).desc());
					default -> throw new BusinessException(ErrorCode.EXPENSE_INVALID_SORT);
				}
			}
		}

		if (orderSpecifiers.isEmpty()) {
			return new OrderSpecifier<?>[] {expense.occurredAt.desc()};
		}

		return orderSpecifiers.toArray(new OrderSpecifier[0]);
	}

	private ConstructorExpression<ExpenseOneShotRow> expenseOneShotProjection(
			QExpenseEntity expense, QTravel travel, QUserCardEntity userCard) {
		return com.querydsl.core.types.Projections.constructor(
				ExpenseOneShotRow.class,
				expense.expenseId,
				expense.accountBookId,
				expense.travelId,
				travel.travelPlaceName,
				travel.imageKey,
				expense.merchant.displayMerchantName,
				expense.exchangeInfo.exchangeRate,
				expense.category,
				expense.occurredAt,
				expense.updatedAt,
				expense.exchangeInfo.localCurrencyAmount,
				expense.exchangeInfo.localCurrencyCode,
				displayBaseAmountExpression(expense),
				displayBaseCurrencyCodeExpression(expense),
				expense.memo,
				expense.expenseSourceInfo.expenseSource,
				expense.approvalNumber,
				expense.cardNumber,
				expense.expenseSourceInfo.fileLink,
				userCard.userCardId,
				userCard.cardCompany,
				userCard.nickName,
				userCard.cardNumber);
	}

	private EnumExpression<CurrencyCode> displayBaseCurrencyCodeExpression(QExpenseEntity expense) {
		return new CaseBuilder()
				.when(expense.exchangeInfo.baseCurrencyAmount.isNotNull())
				.then(expense.exchangeInfo.baseCurrencyCode)
				.otherwise(expense.exchangeInfo.calculatedBaseCurrencyCode);
	}

	private NumberExpression<BigDecimal> displayBaseAmountExpression(QExpenseEntity expense) {
		return new CaseBuilder()
				.when(expense.exchangeInfo.baseCurrencyAmount.isNotNull())
				.then(expense.exchangeInfo.baseCurrencyAmount)
				.otherwise(expense.exchangeInfo.calculatedBaseCurrencyAmount);
	}

	private boolean hasValues(List<?> values) {
		return values != null && !values.isEmpty();
	}
}
