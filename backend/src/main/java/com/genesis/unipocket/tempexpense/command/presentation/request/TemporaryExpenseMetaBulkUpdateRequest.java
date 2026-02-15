package com.genesis.unipocket.tempexpense.command.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * <b>메타 단위 임시지출 일괄 수정 요청</b>
 */
public record TemporaryExpenseMetaBulkUpdateRequest(
		@NotEmpty List<@Valid TemporaryExpenseMetaBulkUpdateItemRequest> items) {}
