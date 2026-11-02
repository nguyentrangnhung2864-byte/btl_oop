package com.travelplanner.demo.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SavedItineraryResponse(Long id, String name, LocalDateTime savedAt,
                                     LocalDate startDate, int dayCount, int placeCount) {
}