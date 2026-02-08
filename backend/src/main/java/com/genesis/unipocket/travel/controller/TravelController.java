package com.genesis.unipocket.travel.controller;

import com.genesis.unipocket.travel.dto.common.WidgetDto;
import com.genesis.unipocket.travel.dto.request.TravelRequest;
import com.genesis.unipocket.travel.dto.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.dto.response.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.response.TravelResponse;
import com.genesis.unipocket.travel.service.TravelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 폴더 기능")
@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelController {

	private final TravelService travelService;

	@PostMapping
	public ResponseEntity<Void> createTravel(@RequestBody @Valid TravelRequest request) {
		Long travelId = travelService.createTravel(request);
		return ResponseEntity.created(URI.create("/api/travels/" + travelId)).build();
	}

	@GetMapping
	public ResponseEntity<List<TravelResponse>> getTravels(@RequestParam Long accountBookId) {
		return ResponseEntity.ok(travelService.getTravels(accountBookId));
	}

	@GetMapping("/{travelId}")
	public ResponseEntity<TravelDetailResponse> getTravelDetail(@PathVariable Long travelId) {
		return ResponseEntity.ok(travelService.getTravelDetail(travelId));
	}

	@PutMapping("/{travelId}")
	public ResponseEntity<Void> updateTravel(
			@PathVariable Long travelId, @RequestBody @Valid TravelRequest request) {
		travelService.updateTravel(travelId, request);
		return ResponseEntity.ok().build();
	}

	@org.springframework.web.bind.annotation.PatchMapping("/{travelId}")
	public ResponseEntity<Void> patchTravel(
			@PathVariable Long travelId, @RequestBody TravelUpdateRequest request) {
		travelService.patchTravel(travelId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{travelId}")
	public ResponseEntity<Void> deleteTravel(@PathVariable Long travelId) {
		travelService.deleteTravel(travelId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{travelId}/widgets")
	public ResponseEntity<Void> updateWidgets(
			@PathVariable Long travelId, @RequestBody @Valid List<WidgetDto> widgets) {
		travelService.updateWidgets(travelId, widgets);
		return ResponseEntity.ok().build();
	}
}
