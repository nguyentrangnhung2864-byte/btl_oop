package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveItineraryRequest(
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String name
) {
}