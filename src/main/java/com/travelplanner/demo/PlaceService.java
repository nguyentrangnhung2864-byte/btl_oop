package com.travelplanner.demo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

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

    public Place create(PlaceRequest request) {
        validateCoordinates(request.latitude(), request.longitude());
        String name = request.name().trim();
        if (placeRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Địa điểm \"" + name + "\" đã tồn tại");
        }
        Place place = new Place();
        applyRequest(place, request);
        return placeRepository.save(place);
    }

    public Place update(Long id, PlaceRequest request) {
        validateCoordinates(request.latitude(), request.longitude());
        Place place = getById(id); // không có id thì tự báo 404
        String name = request.name().trim();
        if (placeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Địa điểm \"" + name + "\" đã tồn tại");
        }
        applyRequest(place, request);
        return placeRepository.save(place);
    }

    public void delete(Long id) {
        Place place = getById(id);
        try {
            placeRepository.delete(place);
        } catch (DataIntegrityViolationException e) {
            // Xảy ra khi địa điểm đang được dùng trong lịch trình (Module 2)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Địa điểm đang được dùng trong lịch trình, không thể xóa");
        }
    }

    private void applyRequest(Place place, PlaceRequest request) {
        place.setName(request.name().trim());
        place.setCity(request.city().trim());
        place.setRegion(request.region().trim());
        place.setCategory(request.category().trim());
        place.setTeaser(request.teaser() == null || request.teaser().isBlank()
                ? null : request.teaser().trim());
        place.setLatitude(request.latitude());
        place.setLongitude(request.longitude());
    }

    // Tọa độ phải đủ cả hai hoặc bỏ trống cả hai, và nằm trong khu vực Việt Nam
    // (khoảng giá trị này để bắt lỗi nhập ngược vĩ độ và kinh độ)
    private void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Phải nhập cả vĩ độ và kinh độ, hoặc để trống cả hai");
        }
        if (latitude == null) {
            return;
        }
        if (latitude < 5 || latitude > 25) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vĩ độ phải nằm trong khoảng 5 đến 25 (bạn có nhập ngược vĩ độ và kinh độ không?)");
        }
        if (longitude < 100 || longitude > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Kinh độ phải nằm trong khoảng 100 đến 120 (bạn có nhập ngược vĩ độ và kinh độ không?)");
        }
    }

    // Chuỗi rỗng hoặc toàn dấu cách được coi như "không chọn"
    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}