package com.travelplanner.demo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PlaceController {

    private final PlaceService placeService;
    private final AiOverviewService aiOverviewService;

    public PlaceController(PlaceService placeService, AiOverviewService aiOverviewService) {
        this.placeService = placeService;
        this.aiOverviewService = aiOverviewService;
    }

    @GetMapping
    public List<Place> search(@RequestParam(required = false) String region,
                              @RequestParam(required = false) String city,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String keyword) {
        return placeService.search(region, city, category, keyword);
    }

    @GetMapping("/filters")
    public FilterOptionsResponse getFilters() {
        return placeService.getFilterOptions();
    }

    @GetMapping("/{id}")
    public Place getById(@PathVariable Long id) {
        return placeService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Place> create(@Valid @RequestBody PlaceRequest request) {
        Place created = placeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Place update(@PathVariable Long id, @Valid @RequestBody PlaceRequest request) {
        return placeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        placeService.delete(id);
    }

    @PostMapping("/{id}/ai-overview")
    public AiOverviewResponse aiOverview(@PathVariable Long id) {
        return aiOverviewService.getAiOverview(id);
    }
}