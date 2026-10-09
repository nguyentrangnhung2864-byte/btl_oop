package com.travelplanner.demo;

import java.util.List;

public record FilterOptionsResponse(List<String> regions,
                                    List<String> cities,
                                    List<String> categories) {
}