package com.travelplanner.demo.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/days")
public class DayController {

    private final DayService dayService;

    public DayController(DayService dayService) {
        this.dayService = dayService;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllDays() {
        dayService.deleteAllDays();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DayResponse createDay() {
        return dayService.createDay();
    }

    @GetMapping
    public List<DayResponse> getAllDays() {
        return dayService.getAllDays();
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDay(@PathVariable Long id) {
        dayService.deleteDay(id);
    }
    @PutMapping("/order")
    public List<DayResponse> reorderDays(@Valid @RequestBody ReorderDaysRequest request) {
        return dayService.reorderDays(request.dayIds());
    }
}