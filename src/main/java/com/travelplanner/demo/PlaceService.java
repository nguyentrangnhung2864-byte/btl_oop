package com.travelplanner.demo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> search(String region, String city, String category, String keyword) {
        return placeRepository.search(clean(region), clean(city), clean(category), clean(keyword));
    }

    public Place getById(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy địa điểm có id = " + id));
    }

    public FilterOptionsResponse getFilterOptions() {
        return new FilterOptionsResponse(
                placeRepository.findDistinctRegions(),
                placeRepository.findDistinctCities(),
                placeRepository.findDistinctCategories());
    }

    // Chuỗi rỗng hoặc toàn dấu cách được coi như "không chọn"
    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}