package com.genesis.unipocket.tempexpense.command.persistence.repository;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseMetaFileCountRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

		boolean existsByS3Key(String s3Key);

	Optional<File> findByS3Key(String s3Key);

	@Query("SELECT f.s3Key FROM File f WHERE f.s3Key IS NOT NULL")
	List<String> findAllS3Keys();

		List<File> findByTempExpenseMetaId(Long tempExpenseMetaId);

	boolean existsByTempExpenseMetaId(Long tempExpenseMetaId);

	void deleteByTempExpenseMetaId(Long tempExpenseMetaId);

	@Query(
			"SELECT new com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseMetaFileCountRow("
					+ "f.tempExpenseMetaId, COUNT(f.fileId)) "
					+ "FROM File f "
					+ "WHERE f.tempExpenseMetaId IN :metaIds "
					+ "GROUP BY f.tempExpenseMetaId")
	List<TempExpenseMetaFileCountRow> countFilesByTempExpenseMetaIdIn(
			@Param("metaIds") List<Long> metaIds);

		@Query(
			"SELECT f FROM File f "
					+ "JOIN TempExpenseMeta tm ON f.tempExpenseMetaId = tm.tempExpenseMetaId "
					+ "LEFT JOIN TemporaryExpense te ON te.fileId = f.fileId "
					+ "WHERE tm.createdAt < :cutoff "
					+ "AND te.tempExpenseId IS NULL")
	List<File> findStaleUnparsedFiles(@Param("cutoff") LocalDateTime cutoff);
}
