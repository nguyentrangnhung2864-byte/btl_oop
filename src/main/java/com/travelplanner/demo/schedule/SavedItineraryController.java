package com.travelplanner.demo.schedule;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
public class SavedItineraryController {

    private final SavedItineraryService service;

    public SavedItineraryController(SavedItineraryService service) {
        this.service = service;
    }

    @GetMapping
    public List<SavedItineraryResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedItineraryResponse save(@Valid @RequestBody SaveItineraryRequest request) {
        return service.saveCurrent(request.name());
    }

    @PostMapping("/{id}/load")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void load(@PathVariable Long id) {
        service.load(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}