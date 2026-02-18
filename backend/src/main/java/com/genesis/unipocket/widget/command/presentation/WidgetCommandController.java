package com.genesis.unipocket.widget.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.widget.command.facade.WidgetCommandOrchestrator;
import com.genesis.unipocket.widget.command.facade.response.AccountBookWidgetsRes;
import com.genesis.unipocket.widget.command.facade.response.TravelWidgetsRes;
import com.genesis.unipocket.widget.command.presentation.request.WidgetItemRequest;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.widget.common.validate.UserTravelValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "위젯 API")
@RestController
@RequiredArgsConstructor
public class WidgetCommandController {

	private final WidgetCommandOrchestrator widgetCommandOrchestrator;
	private final UserAccountBookValidator userAccountBookValidator;
	private final UserTravelValidator userTravelValidator;

	@Operation(summary = "가계부 위젯 순서 수정", description = "가계부의 위젯 순서 정보를 수정합니다.")
	@PutMapping("/account-books/{accountBookId}/widgets")
	public ResponseEntity<List<WidgetItem>> updateAccountBookWidgets(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid List<WidgetItemRequest> requests) {
		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);

		AccountBookWidgetsRes res =
				widgetCommandOrchestrator.updateAccountBookWidgets(accountBookId, requests);
		return ResponseEntity.ok(res.items());
	}

	@Operation(summary = "여행 위젯 순서 수정", description = "여행의 위젯 순서 정보를 수정합니다.")
	@PutMapping("/account-books/{accountBookId}/travels/{travelId}/widgets")
	public ResponseEntity<List<WidgetItem>> updateTravelWidgets(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long travelId,
			@RequestBody @Valid List<WidgetItemRequest> requests) {
		userAccountBookValidator.validateUserAccountBook(userId, accountBookId);
		userTravelValidator.validateTravelInAccountBook(accountBookId, travelId);

		TravelWidgetsRes res =
				widgetCommandOrchestrator.updateTravelWidgets(travelId, requests);
		return ResponseEntity.ok(res.items());
	}
}
