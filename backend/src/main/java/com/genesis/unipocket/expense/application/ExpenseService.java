package com.genesis.unipocket.expense.application;

import com.genesis.unipocket.expense.application.converter.ExpenseApplicationConverter;
import com.genesis.unipocket.expense.application.dto.ExpenseManualCreateCommand;
import com.genesis.unipocket.expense.persistence.entity.expense.Expense;
import com.genesis.unipocket.expense.persistence.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>지출내역 엔티티 관련 서비스 클래스</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final ExpenseApplicationConverter converter;

	public void createExpense(ExpenseManualCreateCommand command) {
		Expense expense = Expense.manual(converter.toArgs(command));
		expenseRepository.save(expense);
	}
}
