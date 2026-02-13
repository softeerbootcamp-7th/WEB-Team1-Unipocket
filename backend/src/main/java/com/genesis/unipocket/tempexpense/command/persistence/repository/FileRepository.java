package com.genesis.unipocket.tempexpense.command.persistence.repository;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <b>파일 Repository</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

	/**
	 * S3 Key 중복 체크
	 */
	boolean existsByS3Key(String s3Key);

	java.util.Optional<File> findByS3Key(String s3Key);

	List<File> findByS3KeyIn(List<String> s3Keys);

	@Query("SELECT f.s3Key FROM File f WHERE f.s3Key IS NOT NULL")
	List<String> findAllS3Keys();

	/**
	 * 메타 ID로 파일 목록 조회
	 */
	List<File> findByTempExpenseMetaId(Long tempExpenseMetaId);

	void deleteByTempExpenseMetaId(Long tempExpenseMetaId);

	/**
	 * 여러 메타 ID로 파일 목록 조회
	 */
	List<File> findByTempExpenseMetaIdIn(List<Long> tempExpenseMetaIds);

	/**
	 * 일정 시간 경과했지만 파싱되지 않은 파일 조회
	 */
	@Query(
			"SELECT f FROM File f JOIN TempExpenseMeta tm ON f.tempExpenseMetaId ="
				+ " tm.tempExpenseMetaId LEFT JOIN TemporaryExpense te ON te.tempExpenseMetaId ="
				+ " tm.tempExpenseMetaId WHERE tm.createdAt < :cutoff AND te.tempExpenseId IS NULL")
	List<File> findStaleUnparsedFiles(@Param("cutoff") LocalDateTime cutoff);
}
