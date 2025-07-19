package com.landmark.hlss_java.repository;

import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LuggageRepository extends JpaRepository<Luggage, Long> {
    Optional<Luggage> findByVerificationCode(String code);
    List<Luggage> findByStatus(LuggageStatus status);
}
