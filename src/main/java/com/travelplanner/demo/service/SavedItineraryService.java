package com.travelplanner.demo.service;

import com.travelplanner.demo.dto.SavedItineraryDetail;
import com.travelplanner.demo.dto.SavedItineraryResponse;
import com.travelplanner.demo.model.*;
import com.travelplanner.demo.repository.DayRepository;
import com.travelplanner.demo.repository.SavedItineraryRepository;
import com.travelplanner.demo.repository.ScheduleItemRepository;
import com.travelplanner.demo.repository.TripSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SavedItineraryService {

    private static final Long TRIP_ID = 1L;

    private final SavedItineraryRepository savedRepository;
    private final DayRepository dayRepository;
    private final ScheduleItemRepository itemRepository;
    private final TripSettingRepository tripRepository;

    public SavedItineraryService(SavedItineraryRepository savedRepository,
                                 DayRepository dayRepository,
                                 ScheduleItemRepository itemRepository,
                                 TripSettingRepository tripRepository) {
        this.savedRepository = savedRepository;
        this.dayRepository = dayRepository;
        this.itemRepository = itemRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedItineraryResponse> list() {
        return savedRepository.findAllByOrderBySavedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SavedItineraryDetail detail(Long id) {
        SavedItinerary saved = savedRepository.findById(id).orElseThrow(() -> notFound(id));
        List<SavedItineraryDetail.Day> days = new ArrayList<>();
        for (int i = 0; i < saved.getDayCount(); i++) {
            final int index = i;
            List<SavedItineraryDetail.Item> items = saved.getItems().stream()
                    .filter(it -> it.getDayIndex() == index)
                    .sorted(Comparator.comparing(SavedItem::getStartTime))
                    .map(it -> new SavedItineraryDetail.Item(
                            it.getPlace().getId(), it.getPlace().getName(), it.getPlace().getCity(),
                            it.getPlace().getCategory(), it.getStartTime(), it.getDurationHours(), it.getNote()))
                    .toList();
            days.add(new SavedItineraryDetail.Day(i + 1, items));
        }
        return new SavedItineraryDetail(saved.getId(), saved.getName(), saved.getSavedAt(), saved.getStartDate(), days);
    }

    /** Lưu lịch trình đang soạn thành một bản mới. */
    @Transactional
    public SavedItineraryResponse saveCurrent(String name) {
        SavedItinerary saved = new SavedItinerary();
        fillFromCurrent(saved, name);
        return toResponse(savedRepository.save(saved));
    }

    /** Ghi đè một bản đã lưu bằng lịch trình đang soạn. */
    @Transactional
    public SavedItineraryResponse updateFromCurrent(Long id, String name) {
        SavedItinerary saved = savedRepository.findById(id).orElseThrow(() -> notFound(id));
        saved.getItems().clear();
        fillFromCurrent(saved, name);
        return toResponse(savedRepository.save(saved));
    }

    private void fillFromCurrent(SavedItinerary saved, String name) {
        List<Day> days = dayRepository.findAllByOrderByDayNumberAsc();
        if (days.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa có ngày nào để lưu");
        }
        saved.setName(name.trim());
        saved.setSavedAt(LocalDateTime.now());
        saved.setStartDate(tripRepository.findById(TRIP_ID).map(TripSetting::getStartDate).orElse(null));
        saved.setDayCount(days.size());
        for (int i = 0; i < days.size(); i++) {
            for (ScheduleItem item : days.get(i).getItems()) {
                SavedItem copy = new SavedItem();
                copy.setItinerary(saved);
                copy.setPlace(item.getPlace());
                copy.setDayIndex(i);
                copy.setStartTime(item.getStartTime());
                copy.setDurationHours(item.getDurationHours());
                copy.setNote(item.getNote());
                saved.getItems().add(copy);
            }
        }
    }

    /** Thay lịch trình đang soạn bằng một bản đã lưu. */
    @Transactional
    public void load(Long id) {
        SavedItinerary saved = savedRepository.findById(id).orElseThrow(() -> notFound(id));

        dayRepository.deleteAll();     // xóa luôn các mục trong ngày (cascade)
        dayRepository.flush();

        List<Day> days = new ArrayList<>();
        for (int i = 0; i < saved.getDayCount(); i++) {
            Day day = new Day();
            day.setDayNumber(i + 1);
            days.add(dayRepository.save(day));
        }
        for (SavedItem s : saved.getItems()) {
            if (s.getDayIndex() < 0 || s.getDayIndex() >= days.size()) continue;
            ScheduleItem item = new ScheduleItem();
            item.setDay(days.get(s.getDayIndex()));
            item.setPlace(s.getPlace());
            item.setStartTime(s.getStartTime());
            item.setDurationHours(s.getDurationHours());
            item.setNote(s.getNote());
            itemRepository.save(item);
        }

        TripSetting setting = tripRepository.findById(TRIP_ID).orElseGet(() -> {
            TripSetting created = new TripSetting();
            created.setId(TRIP_ID);
            return created;
        });
        setting.setStartDate(saved.getStartDate());
        tripRepository.save(setting);
    }

    @Transactional
    public void delete(Long id) {
        if (!savedRepository.existsById(id)) throw notFound(id);
        savedRepository.deleteById(id);
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch trình đã lưu với id " + id);
    }

    private SavedItineraryResponse toResponse(SavedItinerary s) {
        return new SavedItineraryResponse(s.getId(), s.getName(), s.getSavedAt(),
                s.getStartDate(), s.getDayCount(), s.getItems().size());
    }
}