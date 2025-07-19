package com.landmark.hlss_java.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "luggage")
public class Luggage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String guestName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer luggageCount = 1;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime checkinTime;

    private LocalDateTime checkoutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LuggageStatus status = LuggageStatus.STORED;

    @Column(length = 6)
    private String verificationCode;

    // 新增字段
    @Column(length = 20)
    private String storageLocation;

    @Column(length = 255)
    private String qrCodeUrl;

    @Column(length = 10)
    private String handlerStaffId;


}