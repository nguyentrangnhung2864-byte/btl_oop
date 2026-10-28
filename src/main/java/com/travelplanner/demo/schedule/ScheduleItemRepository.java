package com.travelplanner.demo.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByDay_IdOrderByStartTimeAsc(Long dayId);
}