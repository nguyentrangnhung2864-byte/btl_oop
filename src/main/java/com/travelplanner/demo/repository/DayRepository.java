package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DayRepository extends JpaRepository<Day, Long> {
    Optional<Day> findTopByUserIdOrderByDayNumberDesc(Long userId);

    List<Day> findAllByUserIdOrderByDayNumberAsc(Long userId);

    List<Day> findAllByUserId(Long userId);

    // Tìm theo id nhưng chỉ khi ngày đó thuộc về người này
    Optional<Day> findByIdAndUserId(Long id, Long userId);
}