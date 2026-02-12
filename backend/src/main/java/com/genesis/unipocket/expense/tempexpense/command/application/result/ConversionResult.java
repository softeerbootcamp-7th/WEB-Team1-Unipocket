package com.genesis.unipocket.expense.tempexpense.command.application.result;

public record ConversionResult(Long tempExpenseId, Long expenseId, String status, String reason) {}
