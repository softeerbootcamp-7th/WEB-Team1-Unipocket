package com.genesis.unipocket.travel.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.travel.query.persistence.response.TravelDetailQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.service.TravelQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 폴더 기능")
@RestController
@RequestMapping("/api/account-books/{accountBookId}/travels")
@RequiredArgsConstructor
public class TravelQueryController {

	private final TravelQueryService travelQueryService;

	@Operation(summary = "여행 탐색 API", description = "account-bookId 하위의 여행 폴더-메타데이터들을 조회합니다.")
	@GetMapping
	public ResponseEntity<List<TravelQueryResponse>> getTravels(
			@PathVariable Long accountBookId, @LoginUser UUID userId) {
		return ResponseEntity.ok(travelQueryService.getTravels(accountBookId, userId));
	}

	@Operation(summary = "여행 탐색 API", description = "travelId 여행폴더의 메타 데이터를 단건 조회합니다.")
	@GetMapping("/{travelId}")
	public ResponseEntity<TravelDetailQueryResponse> getTravelDetail(
			@PathVariable Long accountBookId, @PathVariable Long travelId, @LoginUser UUID userId) {
		return ResponseEntity.ok(
				travelQueryService.getTravelDetail(accountBookId, travelId, userId));
	}
}
