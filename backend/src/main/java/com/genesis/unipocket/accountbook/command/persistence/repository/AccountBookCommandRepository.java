package com.genesis.unipocket.accountbook.command.persistence.repository;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBookCommandRepository extends JpaRepository<AccountBookEntity, Long> {

    @Query("SELECT a.title FROM AccountBookEntity a WHERE a.userId = :userId AND a.title LIKE"
            + " :titlePrefix%")
    List<String> findNamesStartingWith(
            @Param("userId") String userId, @Param("titlePrefix") String titlePrefix);
}
