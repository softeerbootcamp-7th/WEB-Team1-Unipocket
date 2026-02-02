package com.genesis.unipocket.accountbook.persistence.repository;

import com.genesis.unipocket.accountbook.persistence.entity.AccountBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountBookJpaRepository extends JpaRepository<AccountBookEntity, Long> {

    @Query("SELECT a.title FROM AccountBookEntity a WHERE a.userId = :userId AND a.title LIKE :baseName%")
    List<String> findNamesStartingWith(@Param("userId") Long userId, @Param("baseName") String baseName);
}
