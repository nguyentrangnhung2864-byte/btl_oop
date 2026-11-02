package com.travelplanner.demo.schedule;

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