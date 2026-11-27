package com.travelplanner.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class TripSetting {
    @Id
    private Long id;           // chính là id của người dùng: mỗi người một dòng

    private LocalDate startDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}