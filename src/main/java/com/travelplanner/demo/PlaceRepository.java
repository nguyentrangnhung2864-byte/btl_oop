package com.travelplanner.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlaceRepository {

    List<Place> findByRegion(String region);

    List<Place> findByRegionAndCity(String region, String city);

    List<Place> findByCity(String city);
}
