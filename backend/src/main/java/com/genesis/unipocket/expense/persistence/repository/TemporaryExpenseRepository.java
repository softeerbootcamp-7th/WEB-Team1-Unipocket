package com.genesis.unipocket.expense.persistence.repository;

import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
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

	/**
	 * 파일 ID로 임시지출내역 조회
	 */
	List<TemporaryExpense> findByFileId(Long fileId);

	/**
	 * 가계부 ID로 임시지출내역 조회 (File, TempExpenseMeta 조인)
	 */
	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "JOIN File f ON te.fileId = f.fileId "
					+ "JOIN TempExpenseMeta tm ON f.tempExpenseMetaId = tm.tempExpenseMetaId "
					+ "WHERE tm.accountBookId = :accountBookId")
	List<TemporaryExpense> findByAccountBookId(@Param("accountBookId") Long accountBookId);

	/**
	 * 가계부 ID + 상태로 임시지출내역 조회
	 */
	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "JOIN File f ON te.fileId = f.fileId "
					+ "JOIN TempExpenseMeta tm ON f.tempExpenseMetaId = tm.tempExpenseMetaId "
					+ "WHERE tm.accountBookId = :accountBookId "
					+ "AND te.status = :status")
	List<TemporaryExpense> findByAccountBookIdAndStatus(
			@Param("accountBookId") Long accountBookId,
			@Param("status") TemporaryExpense.TemporaryExpenseStatus status);
}
