package com.landmark.hlss_java.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 行李實體類
 * 對應數據庫中的luggage表，存儲行李相關信息
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "luggage")
public class Luggage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // 行李ID（主鍵）

    @Column(nullable = false)
    private String guestName;       // 客人姓名

    @Column(nullable = false)
    private String phone;           // 客人聯繫電話

    private String roomNumber;      // 房間號（可選）

    @Column(nullable = false)
    private Integer luggageCount;   // 行李數量

    @Column(nullable = false)
    private LocalDateTime checkinTime; // 寄存時間

    private LocalDateTime checkoutTime; // 取件時間（取件時更新）

    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LuggageStatus status;   // 行李狀態（STORED/EXPIRED/RETRIEVED）

    private String storageLocation; // 存放位置（可選）

    private Long handlerStaffId;    // 處理人員ID（可選）

    private Boolean qrGenerated = false; // QR碼是否已生成

    private String verificationCode; // 驗證碼（可選）
}