package com.travelplanner.demo.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SavedItineraryDetail(Long id, String name, LocalDateTime savedAt, LocalDate startDate, List<Day> days) {
    public record Day(int dayNumber, List<Item> items) {
    }

    public record Item(Long placeId, String placeName, String city, String category,
                       LocalTime startTime, double durationHours, String note) {
    }
}