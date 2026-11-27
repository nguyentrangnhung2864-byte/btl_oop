package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    // Chỉ gọi sau khi đã kiểm tra ngày đó thuộc về người dùng
    List<ScheduleItem> findByDay_IdOrderByStartTimeAsc(Long dayId);

    // Tìm theo id nhưng chỉ khi mục đó nằm trong ngày của người này
    Optional<ScheduleItem> findByIdAndDay_UserId(Long id, Long userId);

    // Chỉ các địa điểm người này đã dùng (trước đây lấy của mọi người)
    @Query("select distinct i.place.id from ScheduleItem i where i.day.userId = :userId")
    List<Long> findUsedPlaceIds(@Param("userId") Long userId);
}