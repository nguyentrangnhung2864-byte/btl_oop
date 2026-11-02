package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.*;
import com.travelplanner.demo.service.ScheduleItemService;
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
    public List<ScheduleItemResponse> getItemsByDay(@PathVariable Long dayId) {
        return itemService.getItemsByDay(dayId);
    }

    @PostMapping("/days/{dayId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleItemResponse addItem(@PathVariable Long dayId,
                                        @Valid @RequestBody CreateItemRequest request) {
        return itemService.addItem(dayId, request);
    }

    @PatchMapping("/items/{id}/note")
    public ScheduleItemResponse updateNote(@PathVariable Long id,
                                           @Valid @RequestBody UpdateNoteRequest request) {
        return itemService.updateNote(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @PutMapping("/items/{id}")
    public ScheduleItemResponse updateItem(@PathVariable Long id,
                                           @Valid @RequestBody UpdateItemRequest request) {
        return itemService.updateItem(id, request);
    }
    @PatchMapping("/items/{id}/transport")
    public ScheduleItemResponse updateTransport(@PathVariable Long id,
                                                @Valid @RequestBody UpdateTransportRequest request) {
        return itemService.updateTransport(id, request);
    }
    @GetMapping("/items/place-ids")
    public List<Long> usedPlaceIds() {
        return itemService.getUsedPlaceIds();
    }
}