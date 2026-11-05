package com.travelplanner.demo.service;

import com.travelplanner.demo.model.ScheduleItem;
import com.travelplanner.demo.repository.ScheduleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Module 3: kiểm tra trùng giờ giữa các mục trong cùng một ngày.
 *
 * Hai khung giờ [startA, endA) và [startB, endB) được coi là trùng khi
 *     startA < endB  VÀ  startB < endA
 * Hai mục nối tiếp nhau (mục này kết thúc đúng lúc mục kia bắt đầu) KHÔNG bị tính là trùng.
 */
@Service
public class ScheduleValidationService {

    private final ScheduleItemRepository itemRepository;

    public ScheduleValidationService(ScheduleItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Lấy mục của một ngày từ database rồi trả về id các mục bị trùng giờ. */
    @Transactional(readOnly = true)
    public Set<Long> findConflictIdsByDay(Long dayId) {
        return findConflictIds(itemRepository.findByDay_IdOrderByStartTimeAsc(dayId));
    }

    /** Trả về id của tất cả mục có khung giờ chồng lên một mục khác trong danh sách. */
    public Set<Long> findConflictIds(List<ScheduleItem> items) {
        Set<Long> conflicts = new HashSet<>();
        for (int a = 0; a < items.size(); a++) {
            for (int b = a + 1; b < items.size(); b++) {
                ScheduleItem first = items.get(a);
                ScheduleItem second = items.get(b);
                if (isOverlapping(first, second)) {
                    conflicts.add(first.getId());
                    conflicts.add(second.getId());
                }
            }
        }
        return conflicts;
    }

    /** Hai mục có chồng khung giờ lên nhau không. */
    public boolean isOverlapping(ScheduleItem first, ScheduleItem second) {
        if (first.getStartTime() == null || second.getStartTime() == null) {
            return false;
        }
        int startA = toMinutes(first);
        int endA = startA + durationMinutes(first);
        int startB = toMinutes(second);
        int endB = startB + durationMinutes(second);
        return startA < endB && startB < endA;
    }

    private int toMinutes(ScheduleItem item) {
        return item.getStartTime().getHour() * 60 + item.getStartTime().getMinute();
    }

    private int durationMinutes(ScheduleItem item) {
        return (int) Math.round(item.getDurationHours() * 60);
    }
}
