package com.travelplanner.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // Tiêu chí nào truyền null thì bỏ qua, không lọc theo tiêu chí đó
    @Query("""
            SELECT p FROM Place p
            WHERE (:region IS NULL OR p.region = :region)
              AND (:city IS NULL OR p.city = :city)
              AND (:category IS NULL OR p.category = :category)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.name
            """)
    List<Place> search(@Param("region") String region,
                       @Param("city") String city,
                       @Param("category") String category,
                       @Param("keyword") String keyword);

    @Query("SELECT DISTINCT p.region FROM Place p ORDER BY p.region")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT p.city FROM Place p ORDER BY p.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT p.category FROM Place p ORDER BY p.category")
    List<String> findDistinctCategories();
}