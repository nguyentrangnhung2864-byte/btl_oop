package com.travelplanner.demo.schedule;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trip")
public class TripController {

    private static final Long TRIP_ID = 1L;

    private final TripSettingRepository repository;

    public TripController(TripSettingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public TripResponse get() {
        return new TripResponse(repository.findById(TRIP_ID).map(TripSetting::getStartDate).orElse(null));
    }

    @PutMapping
    @Transactional
    public TripResponse update(@Valid @RequestBody UpdateTripRequest request) {
        TripSetting setting = repository.findById(TRIP_ID).orElseGet(() -> {
            TripSetting created = new TripSetting();
            created.setId(TRIP_ID);
            return created;
        });
        setting.setStartDate(request.startDate());
        return new TripResponse(repository.save(setting).getStartDate());
    }
}