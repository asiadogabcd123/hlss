package com.landmark.hlss_java.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LuggageStatsResponse {
    private int currentStorage;    // 當前寄存數量
    private int todayCheckin;     // 今日寄存數量
    private int todayCheckout;    // 今日取件數量
}
