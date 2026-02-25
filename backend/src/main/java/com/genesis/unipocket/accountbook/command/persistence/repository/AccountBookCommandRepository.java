package com.genesis.unipocket.accountbook.command.persistence.repository;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBookCommandRepository extends JpaRepository<AccountBookEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM AccountBookEntity a JOIN FETCH a.user WHERE a.id = :id")
	Optional<AccountBookEntity> findByIdWithLock(@Param("id") Long id);

	@Query(
			"SELECT a.title FROM AccountBookEntity a WHERE a.user.id = :userId AND a.title LIKE"
					+ " :titlePrefix%")
	List<String> findNamesStartingWith(
			@Param("userId") UUID userId, @Param("titlePrefix") String titlePrefix);

	@Query(
			"SELECT COALESCE(MAX(a.bucketOrder), -1) FROM AccountBookEntity a WHERE a.user.id ="
					+ " :userId")
	Integer findMaxBucketOrderByUserId(@Param("userId") UUID userId);

	Optional<AccountBookEntity> findFirstByUser_IdOrderByBucketOrderAsc(UUID userId);
}
