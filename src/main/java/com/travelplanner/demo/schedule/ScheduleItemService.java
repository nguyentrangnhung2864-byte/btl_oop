package com.travelplanner.demo.schedule;

import com.travelplanner.demo.Place;
import com.travelplanner.demo.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleItemService {

    private final DayRepository dayRepository;
    private final ScheduleItemRepository itemRepository;
    private final PlaceRepository placeRepository;

    public ScheduleItemService(DayRepository dayRepository,
                               ScheduleItemRepository itemRepository,
                               PlaceRepository placeRepository) {
        this.dayRepository = dayRepository;
        this.itemRepository = itemRepository;
        this.placeRepository = placeRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> getItemsByDay(Long dayId) {
        if (!dayRepository.existsById(dayId)) {
            throw notFound("Không tìm thấy ngày với id " + dayId);
        }
        List<ScheduleItem> items = itemRepository.findByDay_IdOrderByStartTimeAsc(dayId);
        List<ScheduleItemResponse> result = new ArrayList<>();
        ScheduleItem previous = null;
        for (ScheduleItem item : items) {
            Double km = previous == null ? null : GeoUtil.distanceKm(
                    previous.getPlace().getLatitude(), previous.getPlace().getLongitude(),
                    item.getPlace().getLatitude(), item.getPlace().getLongitude());
            result.add(toResponse(item, km));
            previous = item;
        }
        return result;
    }

    @Transactional
    public ScheduleItemResponse addItem(Long dayId, CreateItemRequest request) {
        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> notFound("Không tìm thấy ngày với id " + dayId));
        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> notFound("Không tìm thấy địa điểm với id " + request.placeId()));

        ScheduleItem item = new ScheduleItem();
        item.setDay(day);
        item.setPlace(place);
        item.setStartTime(request.startTime());
        item.setDurationHours(request.durationHours());
        item.setNote(request.note());
        return toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<Long> getUsedPlaceIds() {
        return itemRepository.findUsedPlaceIds();
    }

    @Transactional
    public ScheduleItemResponse updateItem(Long id, UpdateItemRequest request) {
        ScheduleItem item = itemRepository.findById(id)
                .orElseThrow(() -> notFound("Không tìm thấy mục với id " + id));
        item.setStartTime(request.startTime());
        item.setDurationHours(request.durationHours());
        item.setNote(request.note());
        return toResponse(item);
    }

    @Transactional
    public ScheduleItemResponse updateNote(Long id, UpdateNoteRequest request) {
        ScheduleItem item = itemRepository.findById(id)
                .orElseThrow(() -> notFound("Không tìm thấy mục với id " + id));
        item.setNote(request.note());
        return toResponse(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw notFound("Không tìm thấy mục với id " + id);
        }
        itemRepository.deleteById(id);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    // Dùng cho thêm, sửa: các thao tác này không tính khoảng cách
    private ScheduleItemResponse toResponse(ScheduleItem item) {
        return toResponse(item, null);
    }

    private ScheduleItemResponse toResponse(ScheduleItem item, Double distanceFromPreviousKm) {
        Place place = item.getPlace();
        return new ScheduleItemResponse(
                item.getId(),
                place.getId(),
                place.getName(),
                place.getCity(),
                place.getRegion(),
                place.getCategory(),
                place.getTeaser(),
                item.getStartTime(),
                item.getDurationHours(),
                item.getNote(),
                item.getTransport(),
                distanceFromPreviousKm);
    }

    @Transactional
    public ScheduleItemResponse updateTransport(Long id, UpdateTransportRequest request) {
        ScheduleItem item = itemRepository.findById(id)
                .orElseThrow(() -> notFound("Không tìm thấy mục với id " + id));
        item.setTransport(request.transport());
        return toResponse(item);
    }
}