package com.travelplanner.demo.service;

import com.travelplanner.demo.dto.DayResponse;
import com.travelplanner.demo.model.Day;
import com.travelplanner.demo.repository.DayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
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
    public DayResponse createDay(Long userId) {
        int next = dayRepository.findTopByUserIdOrderByDayNumberDesc(userId)
                .map(d -> d.getDayNumber() + 1)
                .orElse(1);
        Day day = new Day();
        day.setUserId(userId); // chủ sở hữu lấy từ phiên đăng nhập
        day.setDayNumber(next);
        return toResponse(dayRepository.save(day));
    }

    @Transactional(readOnly = true)
    public List<DayResponse> getAllDays(Long userId) {
        return dayRepository.findAllByUserIdOrderByDayNumberAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private DayResponse toResponse(Day day) {
        return new DayResponse(day.getId(), day.getDayNumber(), day.getItems().size());
    }

    // Chỉ xóa các ngày của chính người này (xóa luôn các mục trong ngày nhờ cascade)
    @Transactional
    public void deleteAllDays(Long userId) {
        dayRepository.deleteAll(dayRepository.findAllByUserId(userId));
    }

    @Transactional
    public void deleteDay(Long id, Long userId) {
        Day day = dayRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy ngày với id " + id));
        dayRepository.delete(day);
        dayRepository.flush();

        // Đánh số lại để không bị hụt ngày (1, 3 -> 1, 2), chỉ trong các ngày của người này
        List<Day> remaining = dayRepository.findAllByUserIdOrderByDayNumberAsc(userId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDayNumber(i + 1);
        }
    }

    @Transactional
    public List<DayResponse> reorderDays(List<Long> dayIds, Long userId) {
        List<Day> days = dayRepository.findAllByUserId(userId);
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