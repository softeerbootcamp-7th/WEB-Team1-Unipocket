package com.genesis.unipocket.travel.command.presentation;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.travel.command.facade.TravelCommandFacade;
import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.command.presentation.request.WidgetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 폴더 기능")
@RestController
@RequestMapping("/api/account-books/{accountBookId}/travels")
@RequiredArgsConstructor
public class TravelCommandController {

	private final TravelCommandFacade travelCommandFacade;

	@Operation(summary = "여행 생성 API", description = "account-bookId 하위에 여행 폴더를 하나 생성합니다.")
	@PostMapping
	public ResponseEntity<Void> createTravel(
			@LoginUser UUID userId, @RequestBody @Valid TravelRequest request) {
		Long travelId = travelCommandFacade.createTravel(request, userId);
		return ResponseEntity.created(
						URI.create("/api/account-books/{account-bookId}/travels/" + travelId))
				.build();
	}

	@Operation(summary = "여행 수정 API", description = "Travel의 메타데이터를 수정합니다.")
	@PutMapping("/{travelId}")
	public ResponseEntity<Void> updateTravel(
			@PathVariable Long travelId,
			@RequestBody @Valid TravelRequest request,
			@LoginUser UUID userId) {
		travelCommandFacade.updateTravel(travelId, request, userId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "여행 수정 API2", description = "Travel의 메타데이터를 수정합니다.")
	@org.springframework.web.bind.annotation.PatchMapping("/{travelId}")
	public ResponseEntity<Void> patchTravel(
			@PathVariable Long travelId,
			@RequestBody TravelUpdateRequest request,
			@LoginUser UUID userId) {
		travelCommandFacade.patchTravel(travelId, request, userId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "여행 삭제 API", description = "여행과 하위의 요소들을 삭제합니다.")
	@DeleteMapping("/{travelId}")
	public ResponseEntity<Void> deleteTravel(@PathVariable Long travelId, @LoginUser UUID userId) {
		travelCommandFacade.deleteTravel(travelId, userId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "위젯 순서 수정 API", description = "여행에 대한 위젯의 순서를 수정합니다.")
	@PutMapping("/{travelId}/widgets")
	public ResponseEntity<Void> updateWidgets(
			@PathVariable Long travelId,
			@RequestBody @Valid List<WidgetRequest> widgets,
			@LoginUser UUID userId) {
		travelCommandFacade.updateWidgets(travelId, widgets, userId);
		return ResponseEntity.ok().build();
	}
}
