package com.genesis.unipocket.expense.tempexpense.command.persistence.repository;

import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.TempExpenseMeta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <b>임시지출내역 메타데이터 Repository</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Repository
public interface TempExpenseMetaRepository extends JpaRepository<TempExpenseMeta, Long> {

	/**
	 * 가계부 ID로 메타데이터 목록 조회
	 */
	List<TempExpenseMeta> findByAccountBookId(Long accountBookId);
}
