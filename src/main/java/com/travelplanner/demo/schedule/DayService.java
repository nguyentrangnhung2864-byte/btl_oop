package com.travelplanner.demo.schedule;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DayService {
    private final DayRepository dayRepository;

    public DayService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }

    @Transactional
    public DayResponse createDay() {
        int next = dayRepository.findTopByOrderByDayNumberDesc()
                .map(d -> d.getDayNumber() + 1)
                .orElse(1);
        Day day = new Day();
        day.setDayNumber(next);
        return toResponse(dayRepository.save(day));
    }

    @Transactional(readOnly = true)
    public List<DayResponse> getAllDays() {
        return dayRepository.findAllByOrderByDayNumberAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private DayResponse toResponse(Day day) {
        return new DayResponse(day.getId(), day.getDayNumber(), day.getItems().size());
    }

    @Transactional
    public void deleteAllDays() {
        dayRepository.deleteAll();
    }

    @Transactional
    public void deleteDay(Long id) {
        if (!dayRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ngày với id " + id);
        }
        dayRepository.deleteById(id);
        dayRepository.flush();

        // Đánh số lại để không bị hụt ngày (1, 3 -> 1, 2)
        List<Day> remaining = dayRepository.findAllByOrderByDayNumberAsc();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDayNumber(i + 1);
        }
    }
    @Transactional
    public List<DayResponse> reorderDays(List<Long> dayIds) {
        List<Day> days = dayRepository.findAll();
        Set<Long> existing = days.stream().map(Day::getId).collect(Collectors.toSet());
        if (dayIds.size() != existing.size() || !existing.equals(new HashSet<>(dayIds))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Danh sách ngày không khớp với dữ liệu hiện có");
        }
        Map<Long, Day> byId = days.stream().collect(Collectors.toMap(Day::getId, d -> d));
        for (int i = 0; i < dayIds.size(); i++) {
            byId.get(dayIds.get(i)).setDayNumber(i + 1);
        }
        return dayIds.stream().map(id -> toResponse(byId.get(id))).toList();
    }
}