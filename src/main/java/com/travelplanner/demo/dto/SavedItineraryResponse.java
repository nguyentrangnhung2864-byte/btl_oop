package com.travelplanner.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SavedItineraryResponse(Long id, String name, LocalDateTime savedAt,
                                     LocalDate startDate, int dayCount, int placeCount) {
}