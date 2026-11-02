package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DayRepository extends JpaRepository<Day, Long> {
    Optional<Day> findTopByOrderByDayNumberDesc();
    List<Day> findAllByOrderByDayNumberAsc();
}