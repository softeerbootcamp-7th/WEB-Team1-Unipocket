package com.genesis.unipocket.tempexpense.query.persistence.response;

import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;

public record TemporaryExpenseMetaStatusCountRow(
		Long tempExpenseMetaId, TemporaryExpenseStatus status, Long expenseCount) {}
