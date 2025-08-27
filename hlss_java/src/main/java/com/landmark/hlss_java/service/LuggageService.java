package com.landmark.hlss_java.service;

import com.landmark.hlss_java.dto.LuggageRequest;
import com.landmark.hlss_java.dto.LuggageStatsResponse;
import com.landmark.hlss_java.dto.RecentLuggage;
import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import com.landmark.hlss_java.exception.ResourceNotFoundException;
import com.landmark.hlss_java.repository.LuggageRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 行李管理服務
 * 處理行李業務邏輯（寄存、查詢、狀態更新等）
 */
@Service
public class LuggageService {
    private static final Logger log = LoggerFactory.getLogger(LuggageService.class);
    private final LuggageRepository repository;
    // 免費寄存天數（僅用於判斷過期狀態）
    private static final int FREE_STORAGE_DAYS = 3;

    @Autowired
    public LuggageService(LuggageRepository repository) {
        this.repository = repository;
    }

    /**
     * 處理行李寄存業務
     */
    @Transactional
    public Luggage checkIn(LuggageRequest request) {
        log.info("辦理行李寄存：客人={}，手機={}，行李數量={}",
                request.getGuestName(), request.getPhone(), request.getLuggageCount());

        Luggage luggage = new Luggage();
        luggage.setGuestName(request.getGuestName());
        luggage.setPhone(request.getPhone());
        luggage.setRoomNumber(request.getRoomNumber());
        luggage.setLuggageCount(request.getLuggageCount());
        luggage.setStatus(LuggageStatus.STORED);
        luggage.setCheckinTime(LocalDateTime.now()); // 記錄寄存時間
        luggage.setQrGenerated(false); // 初始未生成QR碼

        return repository.save(luggage);
    }

    /**
     * 根據驗證碼查詢行李
     */
    public Luggage findByCode(String code) {
        log.info("根據驗證碼查詢行李：code={}", code);
        return repository.findByVerificationCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "行李記錄不存在"
                ));
    }

    /**
     * 根據ID查詢行李（自動更新過期狀態）
     */
    public Optional<Luggage> findById(Long id) {
        log.info("根據ID查詢行李：id={}", id);
        Optional<Luggage> luggage = repository.findById(id);

        // 自動檢查並更新過期狀態
        luggage.ifPresent(this::updateExpiredStatus);
        return luggage;
    }


    /**
     * 自動更新行李過期狀態
     * 若寄存超過免費天數且未取件，標記為EXPIRED
     */
    private void updateExpiredStatus(Luggage luggage) {
        if (luggage.getStatus() == LuggageStatus.STORED && luggage.getCheckinTime() != null) {
            long storedDays = Duration.between(luggage.getCheckinTime(), LocalDateTime.now()).toDays();
            log.debug("行李寄存天數檢查：id={}，已寄存{}天，免費天數{}天",
                    luggage.getId(), storedDays, FREE_STORAGE_DAYS);

            if (storedDays > FREE_STORAGE_DAYS) {
                log.info("行李已過期，更新狀態：id={}，原狀態={}，新狀態=EXPIRED",
                        luggage.getId(), luggage.getStatus());
                luggage.setStatus(LuggageStatus.EXPIRED);
                repository.save(luggage);
            }
        }
    }

    /**
     * 根據狀態和QR生成狀態查詢行李
     */
    public List<Luggage> findByStatusAndQrGenerated(LuggageStatus status, boolean qrGenerated) {
        log.info("查詢行李：狀態={}，QR生成狀態={}", status, qrGenerated);
        return repository.findByStatusAndQrGenerated(status, qrGenerated);
    }

    /**
     * 根據QR生成狀態查詢行李
     */
    public List<Luggage> findByQrGenerated(boolean qrGenerated) {
        log.info("查詢行李：QR生成狀態={}", qrGenerated);
        return repository.findByQrGenerated(qrGenerated);
    }

    /**
     * 根據狀態、QR生成狀態和日期查詢行李
     */
    public List<Luggage> findByStatusAndQrGeneratedAndDate(LuggageStatus status, boolean qrGenerated, LocalDate date) {
        log.info("查詢行李：狀態={}，QR生成狀態={}，日期={}", status, qrGenerated, date);
        return repository.findByStatusAndQrGeneratedAndDate(status, qrGenerated, date);
    }

    /**
     * 根據QR生成狀態和日期查詢行李
     */
    public List<Luggage> findByQrGeneratedAndDate(boolean qrGenerated, LocalDate date) {
        log.info("查詢行李：QR生成狀態={}，日期={}", qrGenerated, date);
        return repository.findByQrGeneratedAndDate(qrGenerated, date);
    }

    /**
     * 根據狀態、QR生成狀態和日期範圍查詢行李
     */
    public List<Luggage> findByStatusAndQrGeneratedBetweenDates(
            LuggageStatus status, boolean qrGenerated, LocalDate startDate, LocalDate endDate) {

        log.info("查詢行李：狀態={}，QR生成狀態={}，開始日期={}，結束日期={}",
                status, qrGenerated, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return repository.findByStatusAndQrGeneratedBetweenDates(status, qrGenerated, startDateTime, endDateTime);
    }

    /**
     * 根據QR生成狀態和日期範圍查詢行李
     */
    public List<Luggage> findByQrGeneratedBetweenDates(
            boolean qrGenerated, LocalDate startDate, LocalDate endDate) {

        log.info("查詢行李：QR生成狀態={}，開始日期={}，結束日期={}",
                qrGenerated, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return repository.findByQrGeneratedBetweenDates(qrGenerated, startDateTime, endDateTime);
    }

    /**
     * 標記行李為已生成QR碼
     */
    @Transactional
    public void markAsQrGenerated(Long luggageId) {
        log.info("標記行李為已生成QR碼：id={}", luggageId);
        Luggage luggage = repository.findById(luggageId)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在"));

        luggage.setQrGenerated(true);
        repository.save(luggage);
        log.info("QR碼生成狀態更新成功：id={}", luggageId);
    }

    /**
     * 根據驗證碼查詢活躍行李（寄存中）
     */
    public Optional<Luggage> getActiveLuggageByCode(String code) {
        log.info("查詢活躍行李：code={}", code);
        return repository.findByVerificationCodeAndStatus(code, LuggageStatus.STORED);
    }

    /**
     * 獲取全部當前寄存的行李（未取件狀態）
     * 包含：已寄存(STORED)、過期未取(EXPIRED)等狀態
     */
    public List<Luggage> findAllCurrentLuggage() {
        log.info("查詢全部當前寄存的行李（未取件狀態）");

        // 定義「當前寄存」包含的狀態（根據實際業務調整）
        List<LuggageStatus> currentStatuses = List.of(
                LuggageStatus.STORED,    // 已寄存
                LuggageStatus.EXPIRED    // 過期未取
                // 如有其他未取件狀態（如PENDING_PICKUP），請一併加入
        );

        return repository.findByStatusIn(currentStatuses);
    }

    // ==================================================
    // 新增：今日寄存/取件專用查詢方法（不影響原有功能）
    // ==================================================
    /**
     * 專用：查詢指定日期的「今日寄存」記錄
     * 條件：已生成QR標籤 + 未取件（STORED/EXPIRED） + 當日寄存時間
     */
    public List<Luggage> getTodayStoredLuggage(LocalDate date) {
        log.info("專用查詢：今日寄存記錄，日期={}", date);

        // 時間區間：當日00:00:00 至 23:59:59
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 未取件狀態列表（排除已取件）
        List<LuggageStatus> storedStatus = Arrays.asList(LuggageStatus.STORED, LuggageStatus.EXPIRED, LuggageStatus.RETRIEVED);

        // 調用Repository新方法查詢
        return repository.findByQrGeneratedAndStatusInAndCheckinTimeBetween(
                storedStatus,  // 未取件狀態
                start,         // 當日開始時間
                end            // 當日結束時間
        );
    }

    /**
     * 專用：查詢指定日期的「今日取件」記錄
     * 條件：已生成QR標籤 + 已取件（RETRIEVED） + 當日取件時間
     */
    public List<Luggage> getTodayRetrievedLuggage(LocalDate date) {
        log.info("專用查詢：今日取件記錄，日期={}", date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 調用Repository新方法查詢
        return repository.findByQrGeneratedAndStatusAndCheckoutTimeBetween(
                LuggageStatus.RETRIEVED, // 已取件狀態
                start,                 // 當日開始時間
                end                    // 當日結束時間
        );
    }
    // ==================================================

    /**
     * 獲取行李統計數據
     */
    public LuggageStatsResponse getStats() {
        log.info("計算行李統計數據");

        // 當前寄存數量
        int current = repository.countByStatus(LuggageStatus.STORED) + repository.countByStatus(LuggageStatus.EXPIRED);

        // 今日寄存數量
        LocalDate today = LocalDate.now();
        int todayIn = repository.countByCheckinTimeBetween(
                today.atStartOfDay(), today.atTime(23, 59, 59));

        // 今日取件數量
        int todayOut = repository.countByStatusAndCheckoutTimeBetween(
                LuggageStatus.RETRIEVED,
                today.atStartOfDay(), today.atTime(23, 59, 59));



        return LuggageStatsResponse.builder()
                .currentStorage(current)
                .todayCheckin(todayIn)
                .todayCheckout(todayOut)
                .build();
    }


    /**
     * 將過期行李標記為「已刪除」狀態（僅狀態更新，保留記錄）
     */
    @Transactional
    public boolean markAsDeleted(Long id) {
        log.info("執行標記刪除行李：id={}", id);
        Luggage luggage = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在"));

        // 僅允許將「已過期」狀態的行李標記為刪除
        if (luggage.getStatus() != LuggageStatus.EXPIRED) {
            log.warn("標記失敗：行李狀態不是過期，id={}，當前狀態={}", id, luggage.getStatus());
            return false;
        }

        // 更新狀態為 DELETE
        luggage.setStatus(LuggageStatus.DELETE);
        repository.save(luggage);
        log.info("行李已標記為刪除狀態：id={}", id);
        return true;
    }

    /**
     * 處理取件業務邏輯（移除數量匹配限制，信任前端驗證）
     */
    @Transactional
    public boolean retrieveLuggage(Long id, List<String> scannedLuggage, String idCardNumber) {
        log.info("處理行李取件：id={}，掃描標籤數量={}，身份證={}",
                id, scannedLuggage.size(),
                idCardNumber != null ? "已提供" : "未提供");

        // 1. 查詢行李並校驗存在性
        Luggage luggage = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在"));

        // 2. 校驗取件狀態（已取件不可重複操作）
        if (luggage.getStatus() == LuggageStatus.RETRIEVED) {
            throw new IllegalStateException("此行李已完成取件，無需重複操作");
        }

        // 3. 移除「掃描數量與訂單匹配」的檢查邏輯
        // （前端已完成驗證，後端直接執行取件）
        log.debug("跳過行李數量匹配檢查，前端已完成驗證：訂單記錄數量={}，實際掃描數量={}",
                Optional.ofNullable(luggage.getLuggageCount()).orElse(1), scannedLuggage.size());

        // 4. 如果有提供身份證號碼，記錄到數據庫
        if (idCardNumber != null && !idCardNumber.trim().isEmpty()) {
            String trimmedIdCard = idCardNumber.trim();
            luggage.setIdNumber(trimmedIdCard); // 使用現有的idNumber字段存儲身份證
            log.info("記錄身份證號碼到行李記錄：行李ID={}, 身份證={}", id, trimmedIdCard);
        }

        // 5. 更新行李狀態為「已取件」並記錄取件時間
        luggage.setStatus(LuggageStatus.RETRIEVED);
        luggage.setCheckoutTime(LocalDateTime.now());
        repository.save(luggage);

        log.info("行李取件成功：id={}, 身份證記錄={}", id, idCardNumber != null ? "是" : "否");
        return true;
    }
}