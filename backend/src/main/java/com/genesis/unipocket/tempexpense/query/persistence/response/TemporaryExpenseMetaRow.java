package com.genesis.unipocket.tempexpense.query.persistence.response;

import java.time.LocalDateTime;

public record TemporaryExpenseMetaRow(Long tempExpenseMetaId, LocalDateTime createdAt) {}
