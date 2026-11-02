package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.SavedItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedItineraryRepository extends JpaRepository<SavedItinerary, Long> {
    List<SavedItinerary> findAllByOrderBySavedAtDesc();
}