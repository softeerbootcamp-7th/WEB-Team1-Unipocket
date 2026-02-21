package com.genesis.unipocket.tempexpense.command.application.parsing.dto;

import java.math.BigDecimal;

public record TemporaryExpenseCalculatedAmount(BigDecimal baseAmount, BigDecimal exchangeRate) {}
