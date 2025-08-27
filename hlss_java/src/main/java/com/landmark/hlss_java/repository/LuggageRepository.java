package com.landmark.hlss_java.repository;

import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 行李數據訪問層
 * 負責與數據庫交互，提供各種查詢和更新操作
 */
public interface LuggageRepository extends JpaRepository<Luggage, Long> {

    // ===================== 基本查詢方法 =====================

    /**
     * 根據驗證碼查詢行李
     */
    Optional<Luggage> findByVerificationCode(String code);

    /**
     * 根據狀態查詢行李（如：所有寄存中的行李）
     */
    List<Luggage> findByStatus(LuggageStatus status);

    /**
     * 根據手機號查詢行李
     */
    List<Luggage> findByPhone(String phone);


    // ===================== 自定義條件查詢 =====================

    /**
     * 查詢未取件且未過期的行李（用於活躍行李統計）
     */
    @Query("SELECT l FROM Luggage l WHERE l.status = 'STORED' AND l.checkinTime > :expiryTime")
    List<Luggage> findActiveLuggage(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * 根據手機號和狀態查詢行李（如：某用戶的所有寄存行李）
     */
    List<Luggage> findByPhoneAndStatus(String phone, LuggageStatus status);

    /**
     * 根據驗證碼和狀態查詢行李（如：未取件的行李）
     */
    Optional<Luggage> findByVerificationCodeAndStatus(String code, LuggageStatus status);

    /**
     * 根據ID和狀態查詢行李（防止修改已取件的行李）
     */
    Optional<Luggage> findByIdAndStatus(Long id, LuggageStatus status);

    /**
     * 根據「狀態、QR生成狀態、日期」查詢
     */
    @Query("SELECT l FROM Luggage l WHERE l.status = :status AND l.qrGenerated = :qrGenerated AND DATE(l.checkinTime) = :date")
    List<Luggage> findByStatusAndQrGeneratedAndDate(
            @Param("status") LuggageStatus status,
            @Param("qrGenerated") boolean qrGenerated,
            @Param("date") LocalDate date);

    /**
     * 根據「QR生成狀態、日期」查詢
     */
    @Query("SELECT l FROM Luggage l WHERE l.qrGenerated = :qrGenerated AND DATE(l.checkinTime) = :date")
    List<Luggage> findByQrGeneratedAndDate(
            @Param("qrGenerated") boolean qrGenerated,
            @Param("date") LocalDate date);

    /**
     * 根據「狀態、QR生成狀態、日期範圍」查詢
     */
    @Query("SELECT l FROM Luggage l WHERE l.status = :status AND l.qrGenerated = :qrGenerated " +
            "AND l.checkinTime BETWEEN :start AND :end")
    List<Luggage> findByStatusAndQrGeneratedBetweenDates(
            @Param("status") LuggageStatus status,
            @Param("qrGenerated") boolean qrGenerated,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 根據「QR生成狀態、日期範圍」查詢
     */
    @Query("SELECT l FROM Luggage l WHERE l.qrGenerated = :qrGenerated " +
            "AND l.checkinTime BETWEEN :start AND :end")
    List<Luggage> findByQrGeneratedBetweenDates(
            @Param("qrGenerated") boolean qrGenerated,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 根據QR生成狀態查詢
     */
    @Query("SELECT l FROM Luggage l WHERE l.qrGenerated = :qrGenerated")
    List<Luggage> findByQrGenerated(@Param("qrGenerated") boolean qrGenerated);


    // ===================== 統計方法 =====================

    /**
     * 根據狀態統計數量（如：當前寄存中的行李數）
     */
    int countByStatus(LuggageStatus status);

    /**
     * 統計某段時間內的寄存數量（如：今日寄存數）
     */
    int countByCheckinTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 統計某段時間內的取件數量（如：今日取件數）
     */
    int countByStatusAndCheckoutTimeBetween(LuggageStatus status, LocalDateTime start, LocalDateTime end);


    // ===================== 排序查詢 =====================

    /**
     * 查詢最近5筆寄存記錄（用於統計頁顯示）
     */
    List<Luggage> findTop5ByOrderByCheckinTimeDesc();

    /**
     * 查詢某段時間內的寄存記錄（如：今日所有寄存）
     */
    List<Luggage> findByCheckinTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 查詢多種狀態的行李（用於查詢全部當前寄存的行李）
     */
    @Query("SELECT l FROM Luggage l WHERE l.status IN :statusList")
    List<Luggage> findByStatusIn(@Param("statusList") List<LuggageStatus> statusList);

    // ===================== 更新操作 =====================

    /**
     * 更新QR生成狀態（批量操作時可用）
     */
    @Modifying
    @Query("UPDATE Luggage l SET l.qrGenerated = :status WHERE l.id = :id")
    void updateQrGeneratedStatus(@Param("id") Long id, @Param("status") boolean status);

    /**
     * 更新行李狀態與取件時間（批量取件時可用）
     */
    @Modifying
    @Query("UPDATE Luggage l SET l.status = :status, l.checkoutTime = :checkoutTime WHERE l.id = :id")
    int updateLuggageStatus(
            @Param("id") Long id,
            @Param("status") LuggageStatus status,
            @Param("checkoutTime") LocalDateTime checkoutTime);

    /**
     * 根據「狀態、QR生成狀態」查詢（如：寄存中且未生成QR的行李）
     */
    List<Luggage> findByStatusAndQrGenerated(LuggageStatus status, boolean qrGenerated);

    // ==================================================
    // 新增：今日寄存/取件專用查詢接口（對應Service新方法）
    // ==================================================
    /**
     * 專用：已生成QR + 多種狀態 + 寄存時間在區間內（今日寄存用）
     */
    @Query("SELECT l FROM Luggage l WHERE  " +
            " l.status IN :statusList " +
            "AND l.checkinTime BETWEEN :start AND :end")
    List<Luggage> findByQrGeneratedAndStatusInAndCheckinTimeBetween(
            @Param("statusList") List<LuggageStatus> statusList,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 專用：已生成QR + 已取件狀態 + 取件時間在區間內（今日取件用）
     */
    @Query("SELECT l FROM Luggage l WHERE " +
            "l.status = :status " +
            "AND l.checkoutTime BETWEEN :start AND :end")
    List<Luggage> findByQrGeneratedAndStatusAndCheckoutTimeBetween(
            @Param("status") LuggageStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}