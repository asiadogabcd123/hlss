package com.landmark.hlss_java.util;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReasons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 動態印表機管理器（穩定兼容版，移除不存在的列印任務查詢API）
 */
@Component
public class PrinterManager {
    private static final Logger log = LoggerFactory.getLogger(PrinterManager.class);
    // 當前活躍的印表機（必須可用）
    private volatile PrintService activePrinter;
    // 系統中所有可用印表機
    private final List<PrintService> availablePrinters = new ArrayList<>();

    // 初始化：服務啟動時加載所有印表機
    public PrinterManager() {
        refreshPrinters();
    }

    /**
     * 刷新系統印表機列表（重新檢查狀態）
     */
    public void refreshPrinters() {
        availablePrinters.clear();
        // 獲取系統中所有印表機
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            availablePrinters.add(service);
            // 判斷印表機是否可用
            boolean isAvailable = isPrinterAvailable(service);
            String status = isAvailable ? "（可用）" : "（不可用）";
            log.info("偵測到印表機：{} {}", service.getName(), status);
        }
        // 默認使用系統默認印表機（若可用）
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrinter != null && isPrinterAvailable(defaultPrinter)) {
            activePrinter = defaultPrinter;
            log.info("預設活躍印表機：{}", defaultPrinter.getName());
        } else {
            activePrinter = null;
            log.warn("未找到可用的預設印表機");
        }
    }

    /**
     * 判斷印表機是否可用（核心方法：預檢查印表機狀態，避免提交無效任務）
     */
    /**
     * 判斷印表機是否可用（放寬檢測條件，優先信任系統狀態）
     */
    public boolean isPrinterAvailable(PrintService printer) {
        try {
            // 關鍵修正1：先判斷印表機是否被系統標記為「可用」（核心）
            if (printer == null) {
                return false;
            }

            // 關鍵修正2：放寬狀態判斷，只排除明確的「錯誤/離線」狀態
            Attribute stateAttr = printer.getAttribute(PrinterState.class);
            if (stateAttr != null) {
                String stateDesc = stateAttr.toString().toLowerCase();
                // 只當包含「error」（錯誤）、「offline」（離線）時才視為不可用
                // 移除「stopped」的判斷，避免誤判暫時閒置的印表機
                if (stateDesc.contains("error") || stateDesc.contains("offline")) {
                    log.debug("印表機「{}」狀態異常：{}", printer.getName(), stateDesc);
                    return false;
                }
            }

            // 關鍵修正3：簡化錯誤原因檢測，只排除嚴重錯誤
            Attribute reasonsAttr = printer.getAttribute(PrinterStateReasons.class);
            if (reasonsAttr != null) {
                String reasonsDesc = reasonsAttr.toString().toLowerCase();
                // 只當包含「no_paper」（無紙）、「paper_jam」（卡紙）、「out_of_toner」（缺墨）時才視為不可用
                // 移除「error」的模糊匹配，避免誤判
                if (reasonsDesc.contains("no_paper")
                        || reasonsDesc.contains("paper_jam")
                        || reasonsDesc.contains("out_of_toner")) {
                    log.debug("印表機「{}」不可用原因：{}", printer.getName(), reasonsDesc);
                    return false;
                }
            }

            // 其餘情況（包括狀態未知）均視為可用
            return true;
        } catch (Exception e) {
            // 獲取狀態失敗時，默認為可用（避免因API調用異常導致誤判）
            log.warn("獲取印表機「{}」狀態時出錯，默認為可用", printer.getName(), e);
            return true;
        }
    }

    /**
     * 根據名稱模糊匹配**可用**印表機（忽略大小寫）
     */
    public PrintService findPrinterByName(String printerName) {
        if (printerName == null || printerName.trim().isEmpty()) {
            return null;
        }
        String target = printerName.trim().toLowerCase();
        for (PrintService printer : availablePrinters) {
            // 只匹配可用的印表機
            if (printer.getName().toLowerCase().contains(target)
                    && isPrinterAvailable(printer)) {
                return printer;
            }
        }
        log.warn("未找到名稱包含「{}」的可用印表機", target);
        return null;
    }

    /**
     * 手動切換當前使用的印表機（只切換到可用設備）
     */
    public boolean switchPrinter(String printerName) {
        // 先刷新列表，確保狀態最新
        refreshPrinters();
        PrintService target = findPrinterByName(printerName);
        if (target != null) {
            activePrinter = target;
            log.info("已切換至可用印表機：{}", target.getName());
            return true;
        }
        log.error("切換失敗：未找到名稱包含「{}」的可用印表機", printerName);
        return false;
    }

    /**
     * 手動設置活躍印表機（用於強制切換到默認設備）
     */
    public void setActivePrinter(PrintService printer) {
        if (printer != null && isPrinterAvailable(printer)) {
            this.activePrinter = printer;
            log.info("強制設置活躍印表機：{}", printer.getName());
        } else {
            log.error("無法設置不可用的印表機為活躍狀態");
        }
    }

    /**
     * 列印前預檢查（替代原來的任務查詢，確保印表機能接收任務）
     * @param printerName 印表機名稱
     * @return 是否允許提交列印任務
     */
    public boolean preCheckBeforePrint(String printerName) {
        // 1. 刷新印表機狀態
        refreshPrinters();

        // 2. 查找目標印表機
        PrintService targetPrinter = findPrinterByName(printerName);
        if (targetPrinter == null) {
            log.error("列印預檢查失敗：印表機「{}」不存在或不可用", printerName);
            return false;
        }

        // 3. 再次確認印表機狀態（避免狀態變化）
        if (!isPrinterAvailable(targetPrinter)) {
            log.error("列印預檢查失敗：印表機「{}」當前不可用（可能已關閉/離線）", printerName);
            return false;
        }

        log.info("列印預檢查通過：印表機「{}」狀態正常", printerName);
        return true;
    }

    /**
     * 獲取當前活躍印表機的名稱（用於Chrome列印時指定設備）
     */
    public String getActivePrinterName() {
        if (activePrinter != null) {
            return activePrinter.getName();
        }
        log.warn("當前沒有活躍的可用印表機");
        return null;
    }

    // getter方法
    public PrintService getActivePrinter() {
        return activePrinter;
    }

    public List<PrintService> getAvailablePrinters() {
        return new ArrayList<>(availablePrinters); // 返回副本，避免併發修改
    }
}