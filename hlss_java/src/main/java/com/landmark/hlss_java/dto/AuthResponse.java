package com.landmark.hlss_java.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        String staffName,
        String role
) {}
