package com.landmark.hlss_java.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LuggageResponse {
    private String id;
    private GuestInfo guest;
    private LocalDateTime checkinTime;
    private LocalDateTime dueTime;
    private String status;
    private StorageInfo storage;
    private String handler;
    private String remark;

    @Data
    @Builder
    public static class GuestInfo {
        private String name;
        private String contact;
        private String roomNumber;
    }

    @Data
    @Builder
    public static class StorageInfo {
        private String location;
    }
}