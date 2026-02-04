package com.genesis.unipocket.expense.persistence.repository;

import com.genesis.unipocket.expense.persistence.entity.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {}
