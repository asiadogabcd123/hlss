package com.landmark.hlss_java.controller;

import com.landmark.hlss_java.dto.LuggageRequest;
import com.landmark.hlss_java.dto.LuggageResponse;
import com.landmark.hlss_java.dto.LuggageSimpleResponse;
import com.landmark.hlss_java.dto.RetrieveRequest;
import com.landmark.hlss_java.dto.LuggageStatsResponse;
import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import com.landmark.hlss_java.exception.ResourceNotFoundException;
import com.landmark.hlss_java.service.LuggageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行李管理控制器
 * 處理行李寄存、查詢、取件等所有API請求
 */
@RestController
@RequestMapping("/api/luggage")
@RequiredArgsConstructor
public class LuggageController {
    private static final Logger log = LoggerFactory.getLogger(LuggageController.class);
    private final LuggageService service;

    /**
     * 行李寄存（新增寄存記錄）
     */
    @PostMapping
    public ResponseEntity<LuggageResponse> checkIn(@RequestBody LuggageRequest request) {
        log.info("收到行李寄存請求：客人={}, 行李數量={}", request.getGuestName(), request.getLuggageCount());
        Luggage luggage = service.checkIn(request);
        return ResponseEntity.ok(convertToResponse(luggage));
    }

    /**
     * 獲取行李統計數據（當前寄存數、今日進出數等）
     */
    @GetMapping("/stats")
    public ResponseEntity<LuggageStatsResponse> getStats() {
        log.info("查詢行李統計數據");
        return ResponseEntity.ok(service.getStats());
    }

    // === 工作人員QR標籤管理API ===

    /**
     * 獲取待生成QR標籤的訂單列表 (qr_generated=0)
     */
    @GetMapping("/pending-qr")
    public ResponseEntity<List<LuggageSimpleResponse>> getPendingForQR(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("查詢待生成QR標籤列表：日期={}, 開始日期={}, 結束日期={}", date, startDate, endDate);
        List<Luggage> pendingOrders;

        if (date != null) {
            pendingOrders = service.findByStatusAndQrGeneratedAndDate(LuggageStatus.STORED, false, date);
        } else if (startDate != null && endDate != null) {
            pendingOrders = service.findByStatusAndQrGeneratedBetweenDates(LuggageStatus.STORED, false, startDate, endDate);
        } else {
            pendingOrders = service.findByStatusAndQrGenerated(LuggageStatus.STORED, false);
        }

        return ResponseEntity.ok(
                pendingOrders.stream()
                        .map(this::convertToSimpleResponse)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 獲取已生成QR標籤的訂單列表 (qr_generated=1)
     */
    @GetMapping("/printed-qr")
    public ResponseEntity<List<LuggageSimpleResponse>> getPrintedForQR(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("查詢已生成QR標籤列表：日期={}, 開始日期={}, 結束日期={}", date, startDate, endDate);
        List<Luggage> printedOrders;

        if (date != null) {
            printedOrders = service.findByQrGeneratedAndDate(true, date);
        } else if (startDate != null && endDate != null) {
            printedOrders = service.findByQrGeneratedBetweenDates(true, startDate, endDate);
        } else {
            printedOrders = service.findByQrGenerated(true);
        }

        return ResponseEntity.ok(
                printedOrders.stream()
                        .map(this::convertToSimpleResponse)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 標記行李為已列印QR標籤狀態
     */
    @PutMapping("/{id}/mark-printed")
    public ResponseEntity<?> markAsPrinted(@PathVariable Long id) {
        log.info("標記行李為已列印QR標籤：id={}", id);
        try {
            service.markAsQrGenerated(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.warn("標記QR標籤失敗：行李不存在，id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根據ID獲取行李詳情（供前端掃描客人二維碼後查詢）
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<LuggageSimpleResponse> getLuggageById(@PathVariable Long id) {
        log.info("查詢行李詳情：id={}", id);
        Luggage luggage = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在"));
        return ResponseEntity.ok(convertToSimpleResponse(luggage));
    }

    /**
     * 獲取全部當前寄存的行李（未取件狀態）
     * 用於前端顯示完整的當前寄存列表
     */
    @GetMapping("/current")
    public ResponseEntity<List<LuggageSimpleResponse>> getAllCurrentLuggage() {
        log.info("API請求：獲取全部當前寄存的行李");

        // 調用Service方法查詢數據
        List<Luggage> currentLuggage = service.findAllCurrentLuggage();

        // 轉換為前端需要的響應格式
        List<LuggageSimpleResponse> response = currentLuggage.stream()
                .map(this::convertToSimpleResponse)  // 使用現有的轉換方法
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 根據行李標籤獲取行李資訊（格式：LUG-{id}-{index}）
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<LuggageSimpleResponse> getLuggageByTag(@PathVariable String tag) {
        log.info("根據標籤查詢行李：tag={}", tag);
        String[] parts = tag.split("-");
        if (parts.length < 3 || !"LUG".equals(parts[0])) {
            log.warn("無效的行李標籤格式：{}", tag);
            throw new IllegalArgumentException("無效的行李標籤格式");
        }

        long luggageId;
        try {
            luggageId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            log.warn("無效的行李ID格式：{}", parts[1]);
            throw new IllegalArgumentException("無效的行李ID格式");
        }

        Luggage luggage = service.findById(luggageId)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在"));

        return ResponseEntity.ok(convertToSimpleResponse(luggage));
    }

    /**
     * 將過期行李標記為「已刪除」狀態（僅更新狀態，不實際刪除記錄）
     */
    @PutMapping("/mark-delete/{id}")
    public ResponseEntity<?> markAsDeleted(@PathVariable Long id) {
        log.info("將行李標記為已刪除狀態：id={}", id);
        try {
            boolean updated = service.markAsDeleted(id);
            if (updated) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "只能將過期狀態的行李標記為刪除"));
            }
        } catch (ResourceNotFoundException e) {
            log.warn("標記失敗：行李不存在，id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 完成取件操作（更新行李狀態為已取件）
     */
    @PostMapping("/{id}/retrieve")
    public ResponseEntity<Map<String, Object>> retrieveLuggage(
            @PathVariable Long id,
            @RequestBody RetrieveRequest request) {

        log.info("處理取件請求：id={}, 掃描標籤數量={}, 身份證={}", id,
                request.getScannedLuggage() != null ? request.getScannedLuggage().size() : 0,
                request.getIdCardNumber() != null ? "已提供" : "未提供");

        try {
            // 檢查請求參數
            if (request.getScannedLuggage() == null || request.getScannedLuggage().isEmpty()) {
                throw new IllegalArgumentException("未掃描到任何行李標籤，請重新掃描");
            }

            boolean success = service.retrieveLuggage(id, request.getScannedLuggage(), request.getIdCardNumber());
            return ResponseEntity.ok(Collections.singletonMap("success", success));

        } catch (IllegalStateException e) {
            log.warn("取件狀態錯誤：{}，id={}", e.getMessage(), id);
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("取件參數錯誤：{}，id={}", e.getMessage(), id);
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));

        } catch (ResourceNotFoundException e) {
            log.warn("取件失敗：{}，id={}", e.getMessage(), id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));

        } catch (Exception e) {
            log.error("取件操作異常：id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "取件處理失敗，請稍後重試"));
        }

    }

    // ==================================================
    // 新增：今日寄存/取件專用API接口（獨立於原有接口）
    // ==================================================
    /**
     * 專用API：查詢指定日期的「今日寄存」記錄
     * 路徑：/api/luggage/today-stored
     * 參數：date（必填，格式YYYY-MM-DD）
     * 返回：已生成QR標籤 + 未取件的今日寄存記錄
     */
    @GetMapping("/today-stored")
    public ResponseEntity<List<LuggageSimpleResponse>> getTodayStored(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("專用API請求：今日寄存記錄，日期={}", date);
        List<Luggage> todayStored = service.getTodayStoredLuggage(date);

        // 轉換為前端所需的簡潔響應格式
        List<LuggageSimpleResponse> response = todayStored.stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 專用API：查詢指定日期的「今日取件」記錄
     * 路徑：/api/luggage/today-retrieved
     * 參數：date（必填，格式YYYY-MM-DD）
     * 返回：已生成QR標籤 + 已取件的今日取件記錄
     */
    @GetMapping("/today-retrieved")
    public ResponseEntity<List<LuggageSimpleResponse>> getTodayRetrieved(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("專用API請求：今日取件記錄，日期={}", date);
        List<Luggage> todayRetrieved = service.getTodayRetrievedLuggage(date);

        List<LuggageSimpleResponse> response = todayRetrieved.stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    // ==================================================

    // === 私有輔助方法 ===

    /**
     * 轉換Luggage實體為LuggageSimpleResponse（簡潔返回格式）
     * 已補充：完整手機號、房間號、身份證號、取件時間
     */
    private LuggageSimpleResponse convertToSimpleResponse(Luggage luggage) {
        return LuggageSimpleResponse.builder()
                .id(luggage.getId())
                .guestName(luggage.getGuestName())
                .phone(luggage.getPhone()) // 完整手機號（不脱敏，前端可自行處理）
                .roomNumber(luggage.getRoomNumber()) // 房間號（無則返回null）
                .luggageCount(luggage.getLuggageCount()) // 行李數量
                .checkinTime(luggage.getCheckinTime()) // 寄存時間
                .checkoutTime(luggage.getCheckoutTime()) // 取件時間（已取件才有值）
                .idNumber(luggage.getIdNumber()) // 身份證號（遺失憑證用，無則返回null）
                .status(luggage.getStatus().name()) // 狀態（枚举轉字符串：STORED/RETRIEVED等）
                .build();
    }

    /**
     * 轉換Luggage實體為LuggageResponse（寄存成功返回格式）
     */
    private LuggageResponse convertToResponse(Luggage luggage) {
        LuggageResponse.GuestInfo guestInfo = LuggageResponse.GuestInfo.builder()
                .name(luggage.getGuestName())
                .contact(luggage.getPhone())
                .roomNumber(luggage.getRoomNumber())
                .build();

        LuggageResponse.StorageInfo storageInfo = LuggageResponse.StorageInfo.builder()
                .location(luggage.getStorageLocation())
                .build();

        return LuggageResponse.builder()
                .id("LUG-" + luggage.getId())
                .guest(guestInfo)
                .checkinTime(luggage.getCheckinTime())
                .dueTime(luggage.getCheckinTime().plusHours(24))
                .status(String.valueOf(luggage.getStatus()))
                .storage(storageInfo)
                .build();
    }
}