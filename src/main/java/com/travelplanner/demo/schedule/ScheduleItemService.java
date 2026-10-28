package com.travelplanner.demo.schedule;

import com.travelplanner.demo.Place;
import com.travelplanner.demo.PlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
        return itemRepository.findByDay_IdOrderByStartTimeAsc(dayId).stream()
                .map(this::toResponse)
                .toList();
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

    private ScheduleItemResponse toResponse(ScheduleItem item) {
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
                item.getNote());
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
}