package com.genesis.unipocket.tempexpense.command.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TemporaryExpenseMetaBulkUpdateRequest(
		@NotEmpty List<@Valid TemporaryExpenseMetaBulkUpdateItemRequest> items) {}
