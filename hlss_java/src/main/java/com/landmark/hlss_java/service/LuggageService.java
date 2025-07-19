package com.landmark.hlss_java.service;

import com.landmark.hlss_java.dto.LuggageRequest;
import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import com.landmark.hlss_java.repository.LuggageRepository;
import com.landmark.hlss_java.util.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LuggageService {
    private final LuggageRepository repository;
    private final VerificationCodeGenerator codeGenerator;

    public Luggage checkIn(LuggageRequest request) {
        Luggage luggage = new Luggage();
        luggage.setGuestName(request.getGuestName());
        luggage.setPhone(request.getPhone());
        luggage.setLuggageCount(request.getLuggageCount());
        luggage.setVerificationCode(codeGenerator.generate());
        return repository.save(luggage);
    }

    public Luggage findByCode(String code) {
        return repository.findByVerificationCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "行李記錄不存在"
                ));
    }
}