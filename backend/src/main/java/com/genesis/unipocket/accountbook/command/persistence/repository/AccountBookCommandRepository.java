package com.genesis.unipocket.accountbook.command.persistence.repository;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBookCommandRepository extends JpaRepository<AccountBookEntity, Long> {

	@Query(
			"SELECT a.title FROM AccountBookEntity a WHERE a.user.id = :userId AND a.title LIKE"
					+ " :titlePrefix%")
	List<String> findNamesStartingWith(
			@Param("userId") UUID userId, @Param("titlePrefix") String titlePrefix);

	@Query(
			"SELECT COALESCE(MAX(a.bucketOrder), 0) FROM AccountBookEntity a WHERE a.user.id ="
					+ " :userId")
	Integer findMaxBucketOrderByUserId(@Param("userId") UUID userId);

	long countByUser_Id(UUID userId);

	Optional<AccountBookEntity> findFirstByUser_IdOrderByBucketOrderAsc(UUID userId);
}
