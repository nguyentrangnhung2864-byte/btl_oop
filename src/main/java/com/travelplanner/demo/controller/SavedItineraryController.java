package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.SaveItineraryRequest;
import com.travelplanner.demo.dto.SavedItineraryDetail;
import com.travelplanner.demo.dto.SavedItineraryResponse;
import com.travelplanner.demo.service.SavedItineraryService;
import com.travelplanner.demo.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
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
    public List<SavedItineraryResponse> list(HttpServletRequest request) {
        return service.list(CurrentUser.requireId(request));
    }

    @GetMapping("/{id}")
    public SavedItineraryDetail detail(@PathVariable Long id, HttpServletRequest request) {
        return service.detail(id, CurrentUser.requireId(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedItineraryResponse save(@Valid @RequestBody SaveItineraryRequest body,
                                       HttpServletRequest request) {
        return service.saveCurrent(body.name(), CurrentUser.requireId(request));
    }

    @PutMapping("/{id}")
    public SavedItineraryResponse update(@PathVariable Long id,
                                         @Valid @RequestBody SaveItineraryRequest body,
                                         HttpServletRequest request) {
        return service.updateFromCurrent(id, body.name(), CurrentUser.requireId(request));
    }

    @PostMapping("/{id}/load")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void load(@PathVariable Long id, HttpServletRequest request) {
        service.load(id, CurrentUser.requireId(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id, CurrentUser.requireId(request));
    }
}