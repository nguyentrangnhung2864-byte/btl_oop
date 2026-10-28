package com.travelplanner.demo.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedItineraryRepository extends JpaRepository<SavedItinerary, Long> {
    List<SavedItinerary> findAllByOrderBySavedAtDesc();
}