package com.landmark.hlss_java.dto;

import java.time.LocalDateTime;

public class LuggageResponse {
    private Long id;
    private String guestName;
    private String verificationCode;
    private LocalDateTime checkinTime;
    private Integer luggageCount;
    private String status;

    // 私有构造器（只能通过Builder创建）
    private LuggageResponse(Builder builder) {
        this.id = builder.id;
        this.guestName = builder.guestName;
        this.verificationCode = builder.verificationCode;
        this.checkinTime = builder.checkinTime;
        this.luggageCount = builder.luggageCount;
        this.status = builder.status;
    }

    // ---------------------------
    // Getter 方法（Lombok @Data 的替代）
    // ---------------------------
    public Long getId() {
        return id;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public Integer getLuggageCount() {
        return luggageCount;
    }

    public String getStatus() {
        return status;
    }

    // ---------------------------
    // Builder 静态内部类
    // ---------------------------
    public static class Builder {
        private Long id;
        private String guestName;
        private String verificationCode;
        private LocalDateTime checkinTime;
        private Integer luggageCount;
        private String status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder guestName(String guestName) {
            this.guestName = guestName;
            return this;
        }

        public Builder verificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
            return this;
        }

        public Builder checkinTime(LocalDateTime checkinTime) {
            this.checkinTime = checkinTime;
            return this;
        }

        public Builder luggageCount(Integer luggageCount) {
            this.luggageCount = luggageCount;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public LuggageResponse build() {
            return new LuggageResponse(this);
        }
    }

    // ---------------------------
    // 静态 builder() 入口方法
    // ---------------------------
    public static Builder builder() {
        return new Builder();
    }
}