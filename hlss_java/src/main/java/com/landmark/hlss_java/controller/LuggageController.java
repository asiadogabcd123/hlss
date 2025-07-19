package com.landmark.hlss_java.controller;

import com.landmark.hlss_java.dto.LuggageRequest;
import com.landmark.hlss_java.dto.LuggageResponse;
import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.service.LuggageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/luggage")
@RequiredArgsConstructor
public class LuggageController {
    private final LuggageService service;

    @PostMapping
    public ResponseEntity<LuggageResponse> checkIn(@RequestBody LuggageRequest request) {
        return ResponseEntity.ok(convertToResponse(service.checkIn(request)));
    }

    @GetMapping("/{code}")
    public ResponseEntity<LuggageResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(convertToResponse(service.findByCode(code)));
    }

    private LuggageResponse convertToResponse(Luggage luggage) {
        return LuggageResponse.builder()
                .id(luggage.getId())
                .guestName(luggage.getGuestName())
                .verificationCode(luggage.getVerificationCode())
                .checkinTime(luggage.getCheckinTime())
                .luggageCount(luggage.getLuggageCount())
                .status(luggage.getStatus().toString())
                .build();
    }
}