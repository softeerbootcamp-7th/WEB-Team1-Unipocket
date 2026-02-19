package com.genesis.unipocket.tempexpense.query.presentation.response;

public record TemporaryExpenseFileUrlResponse(String presignedUrl, int expiresInSeconds) {}
