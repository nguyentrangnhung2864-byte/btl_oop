package com.travelplanner.demo.controller;

import com.travelplanner.demo.dto.TripResponse;
import com.travelplanner.demo.dto.UpdateTripRequest;
import com.travelplanner.demo.model.TripSetting;
import com.travelplanner.demo.repository.TripSettingRepository;
import com.travelplanner.demo.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trip")
public class TripController {

    private final TripSettingRepository repository;

    public TripController(TripSettingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public TripResponse get(HttpServletRequest request) {
        Long userId = CurrentUser.requireId(request);
        return new TripResponse(repository.findById(userId).map(TripSetting::getStartDate).orElse(null));
    }

    @PutMapping
    @Transactional
    public TripResponse update(@Valid @RequestBody UpdateTripRequest body, HttpServletRequest request) {
        Long userId = CurrentUser.requireId(request);
        TripSetting setting = repository.findById(userId).orElseGet(() -> {
            TripSetting created = new TripSetting();
            created.setId(userId); // mỗi người một dòng: id chính là id người dùng
            return created;
        });
        setting.setStartDate(body.startDate());
        return new TripResponse(repository.save(setting).getStartDate());
    }
}