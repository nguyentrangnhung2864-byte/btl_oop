package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SuggestionRequest(
        @Size(max = 100, message = "Tên thành phố quá dài") String city,
        @NotNull(message = "Số ngày không được để trống")
        @Min(value = 1, message = "Tối thiểu 1 ngày")
        @Max(value = 5, message = "Tối đa 5 ngày")
        Integer days,
        @Size(max = 300, message = "Mong muốn tối đa 300 ký tự") String preferences,
        @Size(max = 200, message = "Danh sách địa điểm quá dài") List<Long> placeIds
) {
}