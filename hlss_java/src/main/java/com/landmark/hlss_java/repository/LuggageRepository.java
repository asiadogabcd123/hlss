package com.landmark.hlss_java.repository;

import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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



    // ===================== 自定義條件查詢 =====================



    /**
     * 根據驗證碼和狀態查詢行李（如：未取件的行李）
     */
    Optional<Luggage> findByVerificationCodeAndStatus(String code, LuggageStatus status);


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
    @Query("SELECT COUNT(l) FROM Luggage l WHERE l.qrGenerated = true AND l.status IN (:status)")
    int countByStatus(LuggageStatus status);

    /**
     * 統計某段時間內的寄存數量（如：今日寄存數）
     */
    int countByQrGeneratedTrueAndCheckinTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 統計某段時間內的取件數量（如：今日取件數）
     */
    int countByStatusAndCheckoutTimeBetween(LuggageStatus status, LocalDateTime start, LocalDateTime end);


    // ===================== 排序查詢 =====================



    /**
     * 查詢多種狀態的行李（用於查詢全部當前寄存的行李）
     */
    @Query("SELECT l FROM Luggage l WHERE l.qrGenerated = :qrGenerated and l.status IN :statusList")
    List<Luggage> findByStatusIn(            @Param("qrGenerated") boolean qrGenerated,
                                             @Param("statusList") List<LuggageStatus> statusList);

    // ===================== 更新操作 =====================


    /**
     * 根據「狀態、QR生成狀態」查詢（如：寄存中且未生成QR的行李）
     */
    List<Luggage> findByStatusAndQrGenerated(LuggageStatus status, boolean qrGenerated);

    // ==================================================
    // 今日寄存/取件及歷史記錄查詢接口
    // ==================================================
    /**
     * 已生成QR + 多種狀態 + 寄存時間在區間內（今日寄存和歷史寄存用）
     */
    @Query("SELECT l FROM Luggage l WHERE " +
            "l.qrGenerated = :qrGenerated " +
            "AND l.status IN :statusList " +
            "AND l.checkinTime BETWEEN :start AND :end")
    List<Luggage> findByQrGeneratedAndStatusInAndCheckinTimeBetween(
            @Param("qrGenerated") boolean qrGenerated,
            @Param("statusList") List<LuggageStatus> statusList,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 已生成QR + 已取件狀態 + 取件時間在區間內（今日取件和歷史取件用）
     */
    @Query("SELECT l FROM Luggage l WHERE " +
            "l.qrGenerated = :qrGenerated " +
            "AND l.status = :status " +
            "AND l.checkoutTime BETWEEN :start AND :end")
    List<Luggage> findByQrGeneratedAndStatusAndCheckoutTimeBetween(
            @Param("qrGenerated") boolean qrGenerated,
            @Param("status") LuggageStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
