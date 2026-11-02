package com.travelplanner.demo.dto;

import java.time.LocalDate;

public record UpdateTripRequest(LocalDate startDate) {
}