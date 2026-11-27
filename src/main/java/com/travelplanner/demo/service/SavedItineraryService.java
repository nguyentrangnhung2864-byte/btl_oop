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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SavedItineraryService {


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

    // ===== Các lịch trình ĐÃ LƯU: mỗi người chỉ thấy của mình =====

    @Transactional(readOnly = true)
    public List<SavedItineraryResponse> list(Long userId) {
        return savedRepository.findAllByUserIdOrderBySavedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SavedItineraryDetail detail(Long id, Long userId) {
        SavedItinerary saved = getOwned(id, userId);
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

    /** Lưu lịch trình đang soạn thành một bản mới của người này. */
    @Transactional
    public SavedItineraryResponse saveCurrent(String name, Long userId) {
        SavedItinerary saved = new SavedItinerary();
        saved.setUserId(userId); // chủ sở hữu lấy từ phiên đăng nhập
        fillFromCurrent(saved, name, userId);
        return toResponse(savedRepository.save(saved));
    }

    /** Ghi đè một bản đã lưu (của chính người này) bằng lịch trình đang soạn. */
    @Transactional
    public SavedItineraryResponse updateFromCurrent(Long id, String name, Long userId) {
        SavedItinerary saved = getOwned(id, userId);
        saved.getItems().clear();
        fillFromCurrent(saved, name, userId);
        return toResponse(savedRepository.save(saved));
    }

    /** Thay lịch trình đang soạn bằng một bản đã lưu (của chính người này). */
    @Transactional
    public void load(Long id, Long userId) {
        SavedItinerary saved = getOwned(id, userId);

        clearCurrentDays(userId); // xóa luôn các mục trong ngày (cascade)

        List<Day> days = new ArrayList<>();
        for (int i = 0; i < saved.getDayCount(); i++) {
            days.add(newDay(userId, i + 1));
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

        saveCurrentStartDate(userId, saved.getStartDate());
    }

    @Transactional
    public void delete(Long id, Long userId) {
        savedRepository.delete(getOwned(id, userId));
    }

    // MỌI nơi nhận id từ phía người dùng đều phải đi qua hàm này.
    // Không tìm thấy hoặc không phải của mình đều trả 404, để người lạ không dò ra lịch nào tồn tại.
    private SavedItinerary getOwned(Long id, Long userId) {
        return savedRepository.findByIdAndUserId(id, userId).orElseThrow(() -> notFound(id));
    }

    private void fillFromCurrent(SavedItinerary saved, String name, Long userId) {
        List<Day> days = currentDays(userId);
        if (days.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa có ngày nào để lưu");
        }
        saved.setName(name.trim());
        saved.setSavedAt(LocalDateTime.now());
        saved.setStartDate(currentStartDate(userId));
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

    // ===== BẢN ĐANG SOẠN =====
    // Hiện vẫn DÙNG CHUNG cho mọi người (giữ nguyên cách cũ). Vòng 2 sẽ chuyển sang mỗi người một bản:
    // chỉ cần sửa thân của 5 hàm dưới đây (và các Repository, Controller của Day, ScheduleItem, TripSetting).

    private List<Day> currentDays(Long userId) {
        return dayRepository.findAllByUserIdOrderByDayNumberAsc(userId);
    }

    private LocalDate currentStartDate(Long userId) {
        return tripRepository.findById(userId).map(TripSetting::getStartDate).orElse(null);
    }

    private void clearCurrentDays(Long userId) {
        dayRepository.deleteAll(dayRepository.findAllByUserId(userId));
        dayRepository.flush();
    }

    private Day newDay(Long userId, int dayNumber) {
        Day day = new Day();
        day.setUserId(userId);
        day.setDayNumber(dayNumber);
        return dayRepository.save(day);
    }

    private void saveCurrentStartDate(Long userId, LocalDate startDate) {
        TripSetting setting = tripRepository.findById(userId).orElseGet(() -> {
            TripSetting created = new TripSetting();
            created.setId(userId); // mỗi người một dòng
            return created;
        });
        setting.setStartDate(startDate);
        tripRepository.save(setting);
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch trình đã lưu với id " + id);
    }

    private SavedItineraryResponse toResponse(SavedItinerary s) {
        return new SavedItineraryResponse(s.getId(), s.getName(), s.getSavedAt(),
                s.getStartDate(), s.getDayCount(), s.getItems().size());
    }
}