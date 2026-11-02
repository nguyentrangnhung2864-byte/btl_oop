package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.Pattern;

public record UpdateTransportRequest(
        @Pattern(regexp = "CAR|MOTORBIKE|PLANE|BOAT|TRAIN|BICYCLE|BUS", message = "Phương tiện không hợp lệ")
        String transport
) {
}