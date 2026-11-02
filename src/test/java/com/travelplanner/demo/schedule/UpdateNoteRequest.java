package com.travelplanner.demo.schedule;

import jakarta.validation.constraints.Size;

public record UpdateNoteRequest(
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String note
) {
}