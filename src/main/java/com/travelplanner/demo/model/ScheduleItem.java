package com.travelplanner.demo.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class ScheduleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "day_id")
    private Day day;

    @ManyToOne(optional = false)
    @JoinColumn(name = "place_id")
    private Place place;

    private LocalTime startTime;
    private double durationHours;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Day getDay() { return day; }
    public void setDay(Day day) { this.day = day; }
    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }
    @Column(length = 500)
    private String note;
    @Column(length = 20)
    private String transport;

    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}