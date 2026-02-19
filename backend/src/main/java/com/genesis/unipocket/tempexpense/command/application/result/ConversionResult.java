package com.genesis.unipocket.tempexpense.command.application.result;

public record ConversionResult(Long tempExpenseId, Long expenseId, String status, String reason) {}
