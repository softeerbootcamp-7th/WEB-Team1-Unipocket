package com.genesis.unipocket.travel.command.application.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TravelBudgetUpdateResult(
		Long travelId, BigDecimal budget, LocalDateTime budgetCreatedAt) {}
