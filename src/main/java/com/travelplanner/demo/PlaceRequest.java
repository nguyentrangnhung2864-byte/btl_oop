package com.travelplanner.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlaceRequest(
        @NotBlank(message = "Tên địa điểm không được để trống")
        @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
        String name,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        @Size(max = 255, message = "Tỉnh/Thành phố tối đa 255 ký tự")
        String city,

        @NotBlank(message = "Khu vực phải là Miền Bắc, Miền Trung hoặc Miền Nam")
        @Pattern(regexp = "Miền Bắc|Miền Trung|Miền Nam",
                message = "Khu vực phải là Miền Bắc, Miền Trung hoặc Miền Nam")
        String region,

        @NotBlank(message = "Loại hình không được để trống")
        @Size(max = 255, message = "Loại hình tối đa 255 ký tự")
        String category,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String teaser,
        Double latitude,
        Double longitude
) {
}