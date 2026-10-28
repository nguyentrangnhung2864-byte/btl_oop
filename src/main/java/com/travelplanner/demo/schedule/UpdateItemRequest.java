package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateItemRequest(
        @NotNull(message = "startTime không được để trống") LocalTime startTime,
        @NotNull(message = "durationHours không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "durationHours phải lớn hơn 0")
        Double durationHours,
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String note
) {
}