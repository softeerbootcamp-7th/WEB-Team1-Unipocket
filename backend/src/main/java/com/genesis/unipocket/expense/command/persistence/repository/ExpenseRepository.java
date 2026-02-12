package com.genesis.unipocket.expense.command.persistence.repository;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

	Page<ExpenseEntity> findByAccountBookId(Long accountBookId, Pageable pageable);

	@Query(
			"SELECT e FROM ExpenseEntity e WHERE e.accountBookId = :accountBookId "
					+ "AND (:startDate IS NULL OR e.occurredAt >= :startDate) "
					+ "AND (:endDate IS NULL OR e.occurredAt <= :endDate) "
					+ "AND (:category IS NULL OR e.category = :category) "
					+ "AND (:minAmount IS NULL OR e.exchangeInfo.baseCurrencyAmount >= :minAmount) "
					+ "AND (:maxAmount IS NULL OR e.exchangeInfo.baseCurrencyAmount <= :maxAmount) "
					+ "AND (:merchantName IS NULL OR e.merchant.merchantName LIKE %:merchantName% "
					+ "     OR e.merchant.displayMerchantName LIKE %:merchantName%) "
					+ "AND (:travelId IS NULL OR e.travelId = :travelId)")
	Page<ExpenseEntity> findByFilters(
			@Param("accountBookId") Long accountBookId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			@Param("category") Category category,
			@Param("minAmount") BigDecimal minAmount,
			@Param("maxAmount") BigDecimal maxAmount,
			@Param("merchantName") String merchantName,
			@Param("travelId") Long travelId,
			Pageable pageable);

	long countByAccountBookId(Long accountBookId);
}
