package com.travelplanner.demo.schedule;

import com.travelplanner.demo.Place;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class SavedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerary_id")
    private SavedItinerary itinerary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "place_id")
    private Place place;

    private int dayIndex;            // 0 = Ngày 1
    private LocalTime startTime;
    private double durationHours;

    @Column(length = 500)
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SavedItinerary getItinerary() { return itinerary; }
    public void setItinerary(SavedItinerary itinerary) { this.itinerary = itinerary; }
    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }
    public int getDayIndex() { return dayIndex; }
    public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}