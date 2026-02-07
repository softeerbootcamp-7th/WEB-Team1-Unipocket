package com.genesis.unipocket.accountbook.repository;

import com.genesis.unipocket.accountbook.entity.AccountBookEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBookEntity, Long> {
	@Query(
			"SELECT a.title FROM AccountBookEntity a WHERE a.userId = :userId AND a.title LIKE"
					+ " CONCAT(:baseName, '%')")
	List<String> findNamesStartingWith(
			@Param("userId") String userId, @Param("baseName") String baseName);

	List<AccountBookEntity> findAllByUserId(String userId);
}
