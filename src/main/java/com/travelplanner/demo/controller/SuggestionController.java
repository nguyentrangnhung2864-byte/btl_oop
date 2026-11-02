package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.SuggestionRequest;
import com.travelplanner.demo.dto.SuggestionResponse;
import com.travelplanner.demo.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public SuggestionResponse suggest(@Valid @RequestBody SuggestionRequest request) {
        return suggestionService.suggest(request);
    }
}