package com.travelplanner.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
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
}