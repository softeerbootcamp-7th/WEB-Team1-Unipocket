package com.genesis.unipocket.widget.query.presentation;

import com.genesis.unipocket.widget.query.persistence.response.WidgetItemQueryRes;
import com.genesis.unipocket.widget.query.service.WidgetLayoutQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "위젯 순서 조회 API")
@RestController
@RequiredArgsConstructor
public class WidgetLayoutQueryController {

	private final WidgetLayoutQueryService widgetLayoutQueryService;

	@Operation(summary = "가계부 위젯 순서 조회", description = "가계부의 위젯 순서 정보를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/widgets")
	public ResponseEntity<List<WidgetItemQueryRes>> getAccountBookWidgets(
			@PathVariable Long accountBookId) {

		List<WidgetItemQueryRes> result =
				widgetLayoutQueryService.getAccountBookWidgets(accountBookId);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "여행 위젯 순서 조회", description = "여행의 위젯 순서 정보를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/travel/{travelId}/widgets")
	public ResponseEntity<List<WidgetItemQueryRes>> getTravelWidgets(
			@PathVariable Long accountBookId, @PathVariable Long travelId) {

		List<WidgetItemQueryRes> result = widgetLayoutQueryService.getTravelWidgets(travelId);
		return ResponseEntity.ok(result);
	}
}
