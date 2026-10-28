package com.travelplanner.demo.schedule;

import java.time.LocalTime;

public record ScheduleItemResponse(
        Long id,
        Long placeId,
        String placeName,
        String city,
        String region,
        String category,
        String teaser,
        LocalTime startTime,
        double durationHours,
        String note
) {
}