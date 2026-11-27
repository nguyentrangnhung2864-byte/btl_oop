
package com.travelplanner.demo.service;

import com.travelplanner.demo.dto.*;
import com.travelplanner.demo.model.Day;
import com.travelplanner.demo.model.Place;
import com.travelplanner.demo.model.ScheduleItem;
import com.travelplanner.demo.repository.DayRepository;
import com.travelplanner.demo.repository.PlaceRepository;
import com.travelplanner.demo.repository.ScheduleItemRepository;
import com.travelplanner.demo.util.GeoUtil;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleItemService {

    private final DayRepository dayRepository;
    private final ScheduleItemRepository itemRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleValidationService validationService;

    public ScheduleItemService(
            DayRepository dayRepository,
            ScheduleItemRepository itemRepository,
            PlaceRepository placeRepository,
            ScheduleValidationService validationService) {

        this.dayRepository = dayRepository;
        this.itemRepository = itemRepository;
        this.placeRepository = placeRepository;
        this.validationService = validationService;
    }

    // Kiểm tra ngày có thuộc người dùng đang đăng nhập không
    private Day getOwnedDay(Long dayId, Long userId) {
        return dayRepository.findByIdAndUserId(dayId, userId)
                .orElseThrow(() ->
                        notFound("Không tìm thấy ngày với id " + dayId));
    }

    // Kiểm tra mục lịch trình có thuộc người dùng đang đăng nhập không
    private ScheduleItem getOwnedItem(Long id, Long userId) {
        return itemRepository.findByIdAndDay_UserId(id, userId)
                .orElseThrow(() ->
                        notFound("Không tìm thấy mục với id " + id));
    }

    // Lấy danh sách mục lịch trình theo ngày
    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> getItemsByDay(
            Long dayId, Long userId) {

        getOwnedDay(dayId, userId);

        List<ScheduleItem> items =
                itemRepository.findByDay_IdOrderByStartTimeAsc(dayId);

        Set<Long> conflictIds =
                validationService.findConflictIds(items);

        List<ScheduleItemResponse> result = new ArrayList<>();
        ScheduleItem previous = null;

        for (ScheduleItem item : items) {

            Double km = previous == null ? null : GeoUtil.distanceKm(
                    previous.getPlace().getLatitude(),
                    previous.getPlace().getLongitude(),
                    item.getPlace().getLatitude(),
                    item.getPlace().getLongitude()
            );

            String travelWarning =
                    validationService.checkTravel(previous, item, km);

            result.add(toResponse(
                    item,
                    km,
                    conflictIds.contains(item.getId()),
                    travelWarning
            ));

            previous = item;
        }

        return result;
    }

    // Thêm địa điểm vào lịch trình
    @Transactional
    public ScheduleItemResponse addItem(
            Long dayId, CreateItemRequest request, Long userId) {

        Day day = getOwnedDay(dayId, userId);

        // Địa điểm là dữ liệu công khai
        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() ->
                        notFound("Không tìm thấy địa điểm với id "
                                + request.placeId()));

        ScheduleItem item = new ScheduleItem();

        item.setDay(day);
        item.setPlace(place);
        item.setStartTime(request.startTime());
        item.setDurationHours(request.durationHours());
        item.setNote(request.note());

        return toResponse(itemRepository.save(item));
    }

    // Lấy danh sách ID địa điểm đã được sử dụng bởi người dùng
    @Transactional(readOnly = true)
    public List<Long> getUsedPlaceIds(Long userId) {
        return itemRepository.findUsedPlaceIds(userId);
    }

    // Cập nhật mục lịch trình
    @Transactional
    public ScheduleItemResponse updateItem(
            Long id, UpdateItemRequest request, Long userId) {

        ScheduleItem item = getOwnedItem(id, userId);

        item.setStartTime(request.startTime());
        item.setDurationHours(request.durationHours());
        item.setNote(request.note());

        return toResponse(item);
    }

    // Cập nhật ghi chú
    @Transactional
    public ScheduleItemResponse updateNote(
            Long id, UpdateNoteRequest request, Long userId) {

        ScheduleItem item = getOwnedItem(id, userId);

        item.setNote(request.note());

        return toResponse(item);
    }

    // Cập nhật phương tiện di chuyển
    @Transactional
    public ScheduleItemResponse updateTransport(
            Long id, UpdateTransportRequest request, Long userId) {

        ScheduleItem item = getOwnedItem(id, userId);

        item.setTransport(request.transport());

        return toResponse(item);
    }

    // Xóa mục lịch trình
    @Transactional
    public void deleteItem(Long id, Long userId) {

        ScheduleItem item = getOwnedItem(id, userId);

        itemRepository.delete(item);
    }

    // Trả về lỗi 404
    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND, message);
    }

    // Dùng cho thêm, sửa: kiểm tra lại lịch trình sau khi thay đổi
    private ScheduleItemResponse toResponse(ScheduleItem item) {

        itemRepository.flush();

        boolean conflict = validationService
                .findConflictIdsByDay(item.getDay().getId())
                .contains(item.getId());

        return toResponse(item, null, conflict, null);
    }

    // Tạo response đầy đủ thông tin lịch trình
    private ScheduleItemResponse toResponse(
            ScheduleItem item,
            Double distanceFromPreviousKm,
            boolean conflict,
            String travelWarning) {

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
                distanceFromPreviousKm,
                conflict,
                place.getOpenTime(),
                place.getCloseTime(),
                validationService.isOutsideOpeningHours(item),
                travelWarning
        );
    }
}
