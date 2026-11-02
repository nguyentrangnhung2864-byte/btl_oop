package com.travelplanner.demo.repository;

import com.travelplanner.demo.model.TripSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripSettingRepository extends JpaRepository<TripSetting, Long> {
}