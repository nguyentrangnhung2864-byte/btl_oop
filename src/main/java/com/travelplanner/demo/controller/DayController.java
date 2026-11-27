package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.DayResponse;
import com.travelplanner.demo.dto.ReorderDaysRequest;
import com.travelplanner.demo.service.DayService;
import com.travelplanner.demo.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/days")
public class DayController {

    private final DayService dayService;

    public DayController(DayService dayService) {
        this.dayService = dayService;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllDays(HttpServletRequest request) {
        dayService.deleteAllDays(CurrentUser.requireId(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DayResponse createDay(HttpServletRequest request) {
        return dayService.createDay(CurrentUser.requireId(request));
    }

    @GetMapping
    public List<DayResponse> getAllDays(HttpServletRequest request) {
        return dayService.getAllDays(CurrentUser.requireId(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDay(@PathVariable Long id, HttpServletRequest request) {
        dayService.deleteDay(id, CurrentUser.requireId(request));
    }

    @PutMapping("/order")
    public List<DayResponse> reorderDays(@Valid @RequestBody ReorderDaysRequest body,
                                         HttpServletRequest request) {
        return dayService.reorderDays(body.dayIds(), CurrentUser.requireId(request));
    }
}