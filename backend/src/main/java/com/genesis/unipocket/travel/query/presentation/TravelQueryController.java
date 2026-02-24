package com.genesis.unipocket.travel.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.travel.query.application.TravelAmountQueryService;
import com.genesis.unipocket.travel.query.application.TravelQueryService;
import com.genesis.unipocket.travel.query.persistence.response.TravelDetailQueryResponse;
import com.genesis.unipocket.travel.query.presentation.response.TravelAmountResponse;
import com.genesis.unipocket.travel.query.presentation.response.TravelImageViewUrlResponse;
import com.genesis.unipocket.travel.query.presentation.response.TravelListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 폴더 기능")
@RestController
@RequestMapping("/account-books/{accountBookId}/travels")
@RequiredArgsConstructor
public class TravelQueryController {

	private final TravelQueryService travelQueryService;
	private final TravelAmountQueryService travelAmountQueryService;

	@Operation(summary = "여행 탐색 API", description = "account-bookId 하위의 여행 폴더-메타데이터들을 조회합니다.")
	@GetMapping
	public ResponseEntity<List<TravelListItemResponse>> getTravels(
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

	@Operation(summary = "여행 지출 총액 조회", description = "여행의 전체 지출 총액을 로컬/기준 통화로 조회합니다.")
	@GetMapping("/{travelId}/amount")
	public ResponseEntity<TravelAmountResponse> getTravelAmount(
			@PathVariable Long accountBookId, @PathVariable Long travelId, @LoginUser UUID userId) {
		return ResponseEntity.ok(
				travelAmountQueryService.getTravelAmount(
						accountBookId, travelId, userId.toString()));
	}

	@Operation(
			summary = "여행 이미지 열람 URL 발급 API",
			description = "imageKey로 presigned GET URL을 발급합니다.")
	@GetMapping("/image-url")
	public ResponseEntity<TravelImageViewUrlResponse> getTravelImageUrl(
			@PathVariable Long accountBookId,
			@LoginUser UUID userId,
			@RequestParam String imageKey) {
		String presignedUrl =
				travelQueryService.issueTravelImageViewUrl(accountBookId, userId, imageKey);
		int expiresIn = travelQueryService.getTravelImageViewUrlExpirationSeconds();
		return ResponseEntity.ok(new TravelImageViewUrlResponse(imageKey, presignedUrl, expiresIn));
	}
}
