package com.travelplanner.demo.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByDay_IdOrderByStartTimeAsc(Long dayId);
    @Query("select distinct i.place.id from ScheduleItem i")
    List<Long> findUsedPlaceIds();
}