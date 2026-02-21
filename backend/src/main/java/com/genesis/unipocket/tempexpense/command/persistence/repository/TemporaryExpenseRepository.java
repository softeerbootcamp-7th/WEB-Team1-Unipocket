package com.genesis.unipocket.tempexpense.command.persistence.repository;

import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseConversionContextRow;
import com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseMetaStatusCountRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryExpenseRepository extends JpaRepository<TemporaryExpense, Long> {

	List<TemporaryExpense> findByTempExpenseMetaId(Long tempExpenseMetaId);

	void deleteByTempExpenseMetaId(Long tempExpenseMetaId);

	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "WHERE te.tempExpenseId IN :tempExpenseIds "
					+ "AND te.tempExpenseMetaId = :tempExpenseMetaId "
					+ "AND te.fileId = :fileId")
	List<TemporaryExpense> findScopedByIds(
			@Param("tempExpenseIds") List<Long> tempExpenseIds,
			@Param("tempExpenseMetaId") Long tempExpenseMetaId,
			@Param("fileId") Long fileId);

	@Query(
			"SELECT te FROM TemporaryExpense te "
					+ "WHERE te.tempExpenseId = :tempExpenseId "
					+ "AND te.tempExpenseMetaId = :tempExpenseMetaId "
					+ "AND te.fileId = :fileId")
	Optional<TemporaryExpense> findScopedById(
			@Param("tempExpenseId") Long tempExpenseId,
			@Param("tempExpenseMetaId") Long tempExpenseMetaId,
			@Param("fileId") Long fileId);

	@Query(
			"SELECT new"
				+ " com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseConversionContextRow(tm.accountBookId,"
				+ " f.fileType, f.s3Key) FROM TemporaryExpense te JOIN TempExpenseMeta tm ON"
				+ " tm.tempExpenseMetaId = te.tempExpenseMetaId JOIN File f ON f.fileId = te.fileId"
				+ " WHERE te.tempExpenseId = :tempExpenseId AND tm.accountBookId = :accountBookId"
				+ " AND f.tempExpenseMetaId = te.tempExpenseMetaId")
	Optional<TempExpenseConversionContextRow> findConversionContext(
			@Param("accountBookId") Long accountBookId, @Param("tempExpenseId") Long tempExpenseId);

	@Query(
			"SELECT new"
				+ " com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseMetaStatusCountRow(te.tempExpenseMetaId,"
				+ " te.status, COUNT(te.tempExpenseId)) FROM TemporaryExpense te WHERE"
				+ " te.tempExpenseMetaId IN :metaIds GROUP BY te.tempExpenseMetaId, te.status")
	List<TempExpenseMetaStatusCountRow> countByTempExpenseMetaIdInGroupByStatus(
			@Param("metaIds") List<Long> metaIds);
}
