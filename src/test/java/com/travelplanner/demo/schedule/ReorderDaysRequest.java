package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReorderDaysRequest(
        @NotEmpty(message = "Danh sách ngày không được để trống")
        List<@NotNull Long> dayIds
) {
}