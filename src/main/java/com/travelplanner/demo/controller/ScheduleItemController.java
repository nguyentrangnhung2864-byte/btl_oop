package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.*;
import com.travelplanner.demo.service.ScheduleItemService;
import com.travelplanner.demo.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScheduleItemController {

    private final ScheduleItemService itemService;

    public ScheduleItemController(ScheduleItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/days/{dayId}/items")
    public List<ScheduleItemResponse> getItemsByDay(@PathVariable Long dayId, HttpServletRequest request) {
        return itemService.getItemsByDay(dayId, CurrentUser.requireId(request));
    }

    @PostMapping("/days/{dayId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleItemResponse addItem(@PathVariable Long dayId,
                                        @Valid @RequestBody CreateItemRequest body,
                                        HttpServletRequest request) {
        return itemService.addItem(dayId, body, CurrentUser.requireId(request));
    }

    @PatchMapping("/items/{id}/note")
    public ScheduleItemResponse updateNote(@PathVariable Long id,
                                           @Valid @RequestBody UpdateNoteRequest body,
                                           HttpServletRequest request) {
        return itemService.updateNote(id, body, CurrentUser.requireId(request));
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id, HttpServletRequest request) {
        itemService.deleteItem(id, CurrentUser.requireId(request));
    }

    @PutMapping("/items/{id}")
    public ScheduleItemResponse updateItem(@PathVariable Long id,
                                           @Valid @RequestBody UpdateItemRequest body,
                                           HttpServletRequest request) {
        return itemService.updateItem(id, body, CurrentUser.requireId(request));
    }

    @PatchMapping("/items/{id}/transport")
    public ScheduleItemResponse updateTransport(@PathVariable Long id,
                                                @Valid @RequestBody UpdateTransportRequest body,
                                                HttpServletRequest request) {
        return itemService.updateTransport(id, body, CurrentUser.requireId(request));
    }

    @GetMapping("/items/place-ids")
    public List<Long> usedPlaceIds(HttpServletRequest request) {
        return itemService.getUsedPlaceIds(CurrentUser.requireId(request));
    }
}