package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.SavedItinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedItineraryRepository extends JpaRepository<SavedItinerary, Long> {

    // Chỉ lấy lịch trình của một người
    List<SavedItinerary> findAllByUserIdOrderBySavedAtDesc(Long userId);

    // Tìm theo id nhưng chỉ khi lịch trình đó thuộc về người này
    Optional<SavedItinerary> findByIdAndUserId(Long id, Long userId);
}