package com.genesis.unipocket.expense.command.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ExpenseBulkUpdateRequest(@NotEmpty List<@Valid ExpenseBulkUpdateItemRequest> items) {}
