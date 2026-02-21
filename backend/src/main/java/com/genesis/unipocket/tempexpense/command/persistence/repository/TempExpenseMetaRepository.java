package com.genesis.unipocket.tempexpense.command.persistence.repository;

import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempExpenseMetaRepository extends JpaRepository<TempExpenseMeta, Long> {}
