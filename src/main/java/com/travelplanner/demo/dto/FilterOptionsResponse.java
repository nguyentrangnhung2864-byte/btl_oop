package com.travelplanner.demo.dto;

import java.util.List;

public record FilterOptionsResponse(List<String> regions,
                                    List<String> cities,
                                    List<String> categories) {
}