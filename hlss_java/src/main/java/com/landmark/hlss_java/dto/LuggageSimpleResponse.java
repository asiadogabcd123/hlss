// LuggageSimpleResponse.java
package com.landmark.hlss_java.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LuggageSimpleResponse {
    private Long id;
    private String guestName;
    private String phone; // 完整手机号（替换原来的phoneLast4）
    private String roomNumber; // 新增：房间号
    private Integer luggageCount;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime; // 新增：取件时间
    private String idNumber; // 新增：身份证号
    private String status; // 行李状态（STORED/PICKED_UP等）
    private String storageLocation;
    private String remark;

}