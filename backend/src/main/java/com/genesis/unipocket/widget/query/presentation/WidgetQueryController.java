package com.genesis.unipocket.widget.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.widget.query.presentation.request.WidgetQueryRequest;
import com.genesis.unipocket.widget.query.service.WidgetQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "위젯 조회 API")
@RestController
@RequestMapping("/account-books/{accountBookId}")
@RequiredArgsConstructor
public class WidgetQueryController {

	private final WidgetQueryService widgetQueryService;

	@Operation(summary = "가계부 위젯 조회", description = "가계부에 대한 위젯 데이터를 조회합니다.")
	@GetMapping("/widget")
	public ResponseEntity<Object> getWidget(
			@LoginUser UUID userId, @PathVariable Long accountBookId, WidgetQueryRequest request) {

		Object result =
				widgetQueryService.getWidget(
						userId,
						accountBookId,
						null,
						request.widgetType(),
						request.currencyType(),
						request.period());

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "여행 위젯 조회", description = "여행에 대한 위젯 데이터를 조회합니다.")
	@GetMapping("/travels/{travelId}/widget")
	public ResponseEntity<Object> getTravelWidget(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long travelId,
			WidgetQueryRequest request) {

		Object result =
				widgetQueryService.getWidget(
						userId,
						accountBookId,
						travelId,
						request.widgetType(),
						request.currencyType(),
						request.period());

		return ResponseEntity.ok(result);
	}
}
