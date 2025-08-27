package com.landmark.hlss_java.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentLuggage {
    private Long id;
    private String guestName;
    private LocalDateTime checkinTime;
    private String status;
    private String storageLocation;
}
