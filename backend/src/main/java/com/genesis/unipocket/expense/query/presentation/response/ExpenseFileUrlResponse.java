package com.genesis.unipocket.expense.query.presentation.response;

public record ExpenseFileUrlResponse(String presignedUrl, int expiresInSeconds) {}
