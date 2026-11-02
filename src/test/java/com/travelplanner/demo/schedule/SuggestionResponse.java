package com.travelplanner.demo.schedule;

import java.util.List;

public record SuggestionResponse(List<Day> days) {
    public record Day(List<Item> items) {
    }

    public record Item(Long placeId, String placeName, String startTime, double durationHours, String note) {
    }
}