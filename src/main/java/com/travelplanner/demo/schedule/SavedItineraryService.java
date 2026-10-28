package com.travelplanner.demo.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /** Sao chép lịch trình hiện tại thành một bản có tên. */
    @Transactional
    public SavedItineraryResponse saveCurrent(String name) {
        List<Day> days = dayRepository.findAllByOrderByDayNumberAsc();
        if (days.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa có ngày nào để lưu");
        }
        SavedItinerary saved = new SavedItinerary();
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
        return toResponse(savedRepository.save(saved));
    }

    /** Thay lịch trình hiện tại bằng một bản đã lưu. */
    @Transactional
    public void load(Long id) {
        SavedItinerary saved = savedRepository.findById(id)
                .orElseThrow(() -> notFound(id));

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