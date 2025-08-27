package com.landmark.hlss_java.dto;

import lombok.Data;

@Data
public class LuggageRequest {
    private String guestName;
    private String phone;
    private String roomNumber;
    private Integer luggageCount;
}