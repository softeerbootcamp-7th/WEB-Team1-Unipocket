package com.genesis.unipocket.travel.presentation;

import com.genesis.unipocket.travel.dto.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.TravelRequest;
import com.genesis.unipocket.travel.dto.TravelResponse;
import com.genesis.unipocket.travel.dto.WidgetDto;
import com.genesis.unipocket.travel.service.TravelService;
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
            @PathVariable Long travelId,
            @RequestBody com.genesis.unipocket.travel.dto.TravelUpdateRequest request) {
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
