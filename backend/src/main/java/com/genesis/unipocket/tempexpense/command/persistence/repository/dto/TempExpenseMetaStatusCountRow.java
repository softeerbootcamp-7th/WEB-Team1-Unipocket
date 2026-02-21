package com.genesis.unipocket.tempexpense.command.persistence.repository.dto;

import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;

public record TempExpenseMetaStatusCountRow(
		Long tempExpenseMetaId, TemporaryExpenseStatus status, Long expenseCount) {}
