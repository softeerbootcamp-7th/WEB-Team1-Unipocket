package com.genesis.unipocket.expense.command.persistence.repository;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

	Page<ExpenseEntity> findByAccountBookId(Long accountBookId, Pageable pageable);

	@Query(
			"SELECT DISTINCT e.exchangeInfo.localCurrencyCode, FUNCTION('DATE', e.occurredAt) FROM"
					+ " ExpenseEntity e WHERE e.accountBookId = :accountBookId ORDER BY"
					+ " FUNCTION('DATE', e.occurredAt) ASC, e.exchangeInfo.localCurrencyCode ASC")
	List<Object[]> findDistinctLocalCurrencyDatePairsByAccountBookId(
			@Param("accountBookId") Long accountBookId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			"UPDATE ExpenseEntity e SET e.exchangeInfo.baseCurrencyCode = :newBaseCurrencyCode,"
				+ " e.exchangeInfo.baseCurrencyAmount = FUNCTION('ROUND',"
				+ " e.exchangeInfo.localCurrencyAmount * :exchangeRate, 2),"
				+ " e.exchangeInfo.calculatedBaseCurrencyCode = :newBaseCurrencyCode,"
				+ " e.exchangeInfo.calculatedBaseCurrencyAmount = FUNCTION('ROUND',"
				+ " e.exchangeInfo.localCurrencyAmount * :exchangeRate, 2),"
				+ " e.exchangeInfo.exchangeRate = :exchangeRate WHERE e.accountBookId ="
				+ " :accountBookId AND e.exchangeInfo.localCurrencyCode = :localCurrencyCode AND"
				+ " e.occurredAt >= :dayStart AND e.occurredAt < :nextDayStart")
	int bulkUpdateBaseCurrencyByLocalCurrencyAndOccurredAtRange(
			@Param("accountBookId") Long accountBookId,
			@Param("localCurrencyCode") CurrencyCode localCurrencyCode,
			@Param("newBaseCurrencyCode") CurrencyCode newBaseCurrencyCode,
			@Param("exchangeRate") BigDecimal exchangeRate,
			@Param("dayStart") OffsetDateTime dayStart,
			@Param("nextDayStart") OffsetDateTime nextDayStart);

	List<ExpenseEntity> findAllByAccountBookId(Long accountBookId);

	@Query(
			"SELECT e FROM ExpenseEntity e WHERE e.accountBookId = :accountBookId AND (:startDate"
				+ " IS NULL OR e.occurredAt >= :startDate) AND (:endDate IS NULL OR e.occurredAt <"
				+ " :endDate) AND (:category IS NULL OR e.category = :category) AND (:minAmount IS"
				+ " NULL OR COALESCE(e.exchangeInfo.baseCurrencyAmount,"
				+ " e.exchangeInfo.calculatedBaseCurrencyAmount) >= :minAmount) AND (:maxAmount IS"
				+ " NULL OR COALESCE(e.exchangeInfo.baseCurrencyAmount,"
				+ " e.exchangeInfo.calculatedBaseCurrencyAmount) <= :maxAmount) AND (:merchantName"
				+ " IS NULL OR e.merchant.displayMerchantName LIKE %:merchantName%) AND (:travelId"
				+ " IS NULL OR e.travelId = :travelId)")
	Page<ExpenseEntity> findByFilters(
			@Param("accountBookId") Long accountBookId,
			@Param("startDate") OffsetDateTime startDate,
			@Param("endDate") OffsetDateTime endDate,
			@Param("category") Category category,
			@Param("minAmount") BigDecimal minAmount,
			@Param("maxAmount") BigDecimal maxAmount,
			@Param("merchantName") String merchantName,
			@Param("travelId") Long travelId,
			Pageable pageable);

	long countByAccountBookId(Long accountBookId);

	@Query(
			"SELECT MIN(e.occurredAt), MAX(e.occurredAt) FROM ExpenseEntity e WHERE e.accountBookId"
					+ " = :accountBookId")
	Object[] findOccurredAtRangeByAccountBookId(@Param("accountBookId") Long accountBookId);

	@Query(
			"SELECT MIN(e.occurredAt), MAX(e.occurredAt) FROM ExpenseEntity e WHERE e.accountBookId"
					+ " = :accountBookId AND e.travelId = :travelId")
	Object[] findOccurredAtRangeByAccountBookIdAndTravelId(
			@Param("accountBookId") Long accountBookId, @Param("travelId") Long travelId);

	@Query(
			"SELECT e.merchant.displayMerchantName FROM ExpenseEntity e WHERE e.accountBookId ="
					+ " :accountBookId AND e.merchant.displayMerchantName LIKE CONCAT(:prefix, '%')"
					+ " GROUP BY e.merchant.displayMerchantName ORDER BY MAX(e.updatedAt) DESC")
	List<String> findMerchantNameSuggestions(
			@Param("accountBookId") Long accountBookId,
			@Param("prefix") String prefix,
			Pageable pageable);
}
