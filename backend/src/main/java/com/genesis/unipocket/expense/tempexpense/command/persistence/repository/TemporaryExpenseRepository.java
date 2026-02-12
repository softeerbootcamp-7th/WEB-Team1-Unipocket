package com.genesis.unipocket.expense.tempexpense.command.persistence.repository;

import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.TemporaryExpense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <b>임시지출내역 Repository</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Repository
public interface TemporaryExpenseRepository extends JpaRepository<TemporaryExpense, Long> {

	List<TemporaryExpense> findByTempExpenseMetaId(Long tempExpenseMetaId);

	void deleteByTempExpenseMetaId(Long tempExpenseMetaId);

	/**
	 * 가계부 ID로 임시지출내역 조회 (File, TempExpenseMeta 조인)
	 */
	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "JOIN TempExpenseMeta tm ON te.tempExpenseMetaId = tm.tempExpenseMetaId "
					+ "WHERE tm.accountBookId = :accountBookId")
	List<TemporaryExpense> findByAccountBookId(@Param("accountBookId") Long accountBookId);

	/**
	 * 가계부 ID + 상태로 임시지출내역 조회
	 */
	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "JOIN TempExpenseMeta tm ON te.tempExpenseMetaId = tm.tempExpenseMetaId "
					+ "WHERE tm.accountBookId = :accountBookId "
					+ "AND te.status = :status")
	List<TemporaryExpense> findByAccountBookIdAndStatus(
			@Param("accountBookId") Long accountBookId,
			@Param("status") TemporaryExpense.TemporaryExpenseStatus status);

	List<TemporaryExpense> findByTempExpenseMetaIdIn(List<Long> tempExpenseMetaIds);
}
