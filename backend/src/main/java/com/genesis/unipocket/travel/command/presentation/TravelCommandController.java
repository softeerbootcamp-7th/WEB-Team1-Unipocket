package com.genesis.unipocket.travel.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.travel.command.application.command.CreateTravelCommand;
import com.genesis.unipocket.travel.command.application.command.PatchTravelCommand;
import com.genesis.unipocket.travel.command.application.command.UpdateTravelCommand;
import com.genesis.unipocket.travel.command.facade.TravelCommandFacade;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;
import com.genesis.unipocket.travel.command.presentation.request.TravelImageUploadPathRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.command.presentation.response.TravelImageUploadPathResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/account-books/{accountBookId}/travels")
@RequiredArgsConstructor
public class TravelCommandController {

	private final TravelCommandFacade travelCommandFacade;

	@Value("${app.media.presigned-put-expiration-seconds:300}")
	private int presignedPutExpirationSeconds;

	@Operation(summary = "여행 생성 API", description = "account-bookId 하위에 여행 폴더를 하나 생성합니다.")
	@PostMapping
	public ResponseEntity<Void> createTravel(
			@PathVariable Long accountBookId,
			@LoginUser UUID userId,
			@RequestBody @Valid TravelRequest request) {
		CreateTravelCommand command =
				CreateTravelCommand.of(
						accountBookId,
						request.travelPlaceName(),
						request.startDate(),
						request.endDate(),
						request.imageKey());
		Long travelId = travelCommandFacade.createTravel(command, userId);
		return ResponseEntity.created(
						URI.create("/account-books/" + accountBookId + "/travels/" + travelId))
				.build();
	}

	@Operation(
			summary = "여행 이미지 업로드 URL 발급 API",
			description = "여행 이미지 업로드를 위한 presigned PUT URL과 imageKey를 발급합니다.")
	@PostMapping("/images/presigned-url")
	public ResponseEntity<TravelImageUploadPathResponse> issueTravelImageUploadPath(
			@PathVariable Long accountBookId,
			@LoginUser UUID userId,
			@RequestBody @Valid TravelImageUploadPathRequest request) {
		TravelImageUploadPathInfo info =
				travelCommandFacade.issueTravelImageUploadPath(
						accountBookId, userId, request.mimeType());
		return ResponseEntity.ok(
				TravelImageUploadPathResponse.from(info, presignedPutExpirationSeconds));
	}

	@Operation(summary = "여행 수정 API", description = "Travel의 메타데이터를 수정합니다.")
	@PutMapping("/{travelId}")
	public ResponseEntity<Void> updateTravel(
			@PathVariable Long accountBookId,
			@PathVariable Long travelId,
			@RequestBody @Valid TravelRequest request,
			@LoginUser UUID userId) {
		UpdateTravelCommand command =
				UpdateTravelCommand.of(
						travelId,
						request.travelPlaceName(),
						request.startDate(),
						request.endDate(),
						request.imageKey());
		travelCommandFacade.updateTravel(accountBookId, command, userId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "여행 수정 API2", description = "Travel의 메타데이터를 수정합니다.")
	@org.springframework.web.bind.annotation.PatchMapping("/{travelId}")
	public ResponseEntity<Void> patchTravel(
			@PathVariable Long travelId,
			@RequestBody @Valid TravelUpdateRequest request,
			@LoginUser UUID userId) {
		PatchTravelCommand command =
				PatchTravelCommand.of(
						travelId,
						request.travelPlaceName(),
						request.startDate(),
						request.endDate(),
						request.imageKey());
		travelCommandFacade.patchTravel(command, userId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "여행 삭제 API", description = "여행과 하위의 요소들을 삭제합니다.")
	@DeleteMapping("/{travelId}")
	public ResponseEntity<Void> deleteTravel(@PathVariable Long travelId, @LoginUser UUID userId) {
		travelCommandFacade.deleteTravel(travelId, userId);
		return ResponseEntity.noContent().build();
	}
}
