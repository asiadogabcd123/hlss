package com.landmark.hlss_java.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landmark.hlss_java.dto.LuggageRequest;
import com.landmark.hlss_java.dto.LuggageStatsResponse;
import com.landmark.hlss_java.entity.Luggage;
import com.landmark.hlss_java.entity.LuggageStatus;
import com.landmark.hlss_java.exception.ResourceNotFoundException;
import com.landmark.hlss_java.repository.LuggageRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.landmark.hlss_java.util.PrinterManager;
import com.landmark.hlss_java.util.printer.SystemKeySender;
import jakarta.transaction.Transactional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 行李管理服務（新增JSON二維碼、顯式等待、备注字段支持）
 */
@Service
public class LuggageService {
    private static final Logger log = LoggerFactory.getLogger(LuggageService.class);
    private final LuggageRepository repository;
    private final PrinterManager printerManager;
    private static final int FREE_STORAGE_DAYS = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${printer.preferred:}")
    private String preferredPrinterName;

    private static final String HOTEL_LOGO_PATH = "images/new_orient_group.jpg";
    private static final int LABEL_WIDTH_MM = 80;

    private File tempHtmlFile;

    @Autowired
    public LuggageService(LuggageRepository repository, PrinterManager printerManager) {
        this.repository = repository;
        this.printerManager = printerManager;
        initPreferredPrinter();
    }


    private void initPreferredPrinter() {
        printerManager.refreshPrinters();

        if (preferredPrinterName != null && !preferredPrinterName.trim().isEmpty()) {
            boolean switchSuccess = printerManager.switchPrinter(preferredPrinterName);
            if (switchSuccess) {
                log.info("成功初始化優先印表機：{}", preferredPrinterName);
                return;
            }
        }

        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrinter != null) {
            printerManager.setActivePrinter(defaultPrinter);
            log.info("使用系統預設印表機初始化：{}", defaultPrinter.getName());
        } else {
            log.error("初始化失敗：系統無預設印表機，列印功能暫不可用");
        }
    }

    // ====================== 核心業務方法（新增备注保存逻辑） ======================
    @Transactional
    public Luggage checkIn(LuggageRequest request) {
        log.info("辦理行李寄存：客人={}，手機={}，行李數量={}，存放位置={}，备注={}",
                request.getGuestName(), request.getPhone(), request.getLuggageCount(),
                request.getStorageLocation(), request.getRemark());

        Luggage luggage = new Luggage();
        luggage.setGuestName(request.getGuestName());
        luggage.setPhone(request.getPhone());
        luggage.setRoomNumber(request.getRoomNumber());
        luggage.setLuggageCount(request.getLuggageCount());
        luggage.setStatus(LuggageStatus.STORED);
        luggage.setCheckinTime(LocalDateTime.now());
        luggage.setQrGenerated(false);
        luggage.setStorageLocation(request.getStorageLocation());
        luggage.setRemark(request.getRemark() != null ? request.getRemark().trim() : "無");

        return repository.save(luggage);
    }

    public Optional<Luggage> findById(Long id) {
        log.info("根據ID查詢行李：id={}", id);
        Optional<Luggage> luggage = repository.findById(id);
        luggage.ifPresent(this::updateExpiredStatus);
        return luggage;
    }

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

    // 以下查询类方法（findByXXX）保持不变，无需修改
    public List<Luggage> findByStatusAndQrGenerated(LuggageStatus status, boolean qrGenerated) {
        log.info("查詢行李：狀態={}，QR生成狀態={}", status, qrGenerated);
        return repository.findByStatusAndQrGenerated(status, qrGenerated);
    }

    public List<Luggage> findByQrGenerated(boolean qrGenerated) {
        log.info("查詢行李：QR生成狀態={}", qrGenerated);
        return repository.findByQrGenerated(qrGenerated);
    }

    public List<Luggage> findByStatusAndQrGeneratedAndDate(LuggageStatus status, boolean qrGenerated, LocalDate date) {
        log.info("查詢行李：狀態={}，QR生成狀態={}，日期={}", status, qrGenerated, date);
        return repository.findByStatusAndQrGeneratedAndDate(status, qrGenerated, date);
    }

    public List<Luggage> findByQrGeneratedAndDate(boolean qrGenerated, LocalDate date) {
        log.info("查詢行李：QR生成狀態={}，日期={}", qrGenerated, date);
        return repository.findByQrGeneratedAndDate(qrGenerated, date);
    }

    public List<Luggage> findByStatusAndQrGeneratedBetweenDates(
            LuggageStatus status, boolean qrGenerated, LocalDate startDate, LocalDate endDate) {

        log.info("查詢行李：狀態={}，QR生成狀態={}，開始日期={}，結束日期={}",
                status, qrGenerated, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return repository.findByStatusAndQrGeneratedBetweenDates(status, qrGenerated, startDateTime, endDateTime);
    }

    public List<Luggage> findByQrGeneratedBetweenDates(
            boolean qrGenerated, LocalDate startDate, LocalDate endDate) {

        log.info("查詢行李：QR生成狀態={}，開始日期={}，結束日期={}",
                qrGenerated, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return repository.findByQrGeneratedBetweenDates(qrGenerated, startDateTime, endDateTime);
    }

    @Transactional
    public void markAsQrGenerated(Long luggageId) {
        log.info("標記行李為已生成QR碼：id={}", luggageId);
        Luggage luggage = repository.findById(luggageId)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在：ID=" + luggageId));

        luggage.setQrGenerated(true);
        repository.save(luggage);
        log.info("QR碼生成狀態更新成功：id={}", luggageId);
    }

    public List<Luggage> findAllCurrentLuggage() {
        log.info("查詢全部當前寄存的行李（未取件狀態）");
        List<LuggageStatus> currentStatuses = List.of(LuggageStatus.STORED, LuggageStatus.EXPIRED);
        return repository.findByStatusIn(true, currentStatuses);
    }

    public List<Luggage> getTodayStoredLuggage(LocalDate date) {
        log.info("查詢今日寄存記錄，日期={}", date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        List<LuggageStatus> storedStatus = Arrays.asList(LuggageStatus.STORED, LuggageStatus.EXPIRED);
        return repository.findByQrGeneratedAndStatusInAndCheckinTimeBetween(true, storedStatus, start, end);
    }

    public List<Luggage> getTodayRetrievedLuggage(LocalDate date) {
        log.info("查詢今日取件記錄，日期={}", date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return repository.findByQrGeneratedAndStatusAndCheckoutTimeBetween(true, LuggageStatus.RETRIEVED, start, end);
    }

    public List<Luggage> getHistoricalStoredLuggage(LocalDate startDate, LocalDate endDate) {
        log.info("查詢歷史寄存記錄，時間範圍={} 至 {}", startDate, endDate);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<LuggageStatus> storedStatus = Arrays.asList(LuggageStatus.STORED, LuggageStatus.EXPIRED);
        return repository.findByQrGeneratedAndStatusInAndCheckinTimeBetween(true, storedStatus, start, end);
    }

    public List<Luggage> getHistoricalRetrievedLuggage(LocalDate startDate, LocalDate endDate) {
        log.info("查詢歷史取件記錄，時間範圍={} 至 {}", startDate, endDate);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return repository.findByQrGeneratedAndStatusAndCheckoutTimeBetween(true, LuggageStatus.RETRIEVED, start, end);
    }

    public LuggageStatsResponse getStats() {
        log.info("計算行李統計數據");
        int currentStorage = repository.countByStatus(LuggageStatus.STORED) + repository.countByStatus(LuggageStatus.EXPIRED);
        LocalDate today = LocalDate.now();
        int todayCheckin = repository.countByQrGeneratedTrueAndCheckinTimeBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
        int todayCheckout = repository.countByStatusAndCheckoutTimeBetween(LuggageStatus.RETRIEVED, today.atStartOfDay(), today.atTime(23, 59, 59));

        return LuggageStatsResponse.builder()
                .currentStorage(currentStorage)
                .todayCheckin(todayCheckin)
                .todayCheckout(todayCheckout)
                .build();
    }

    @Transactional
    public boolean markAsDeleted(Long id) {
        log.info("執行標記刪除行李：id={}", id);
        Luggage luggage = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在：ID=" + id));

        if (luggage.getStatus() != LuggageStatus.EXPIRED) {
            log.warn("標記刪除失敗：行李狀態非過期，id={}，當前狀態={}", id, luggage.getStatus());
            return false;
        }

        luggage.setStatus(LuggageStatus.DELETE);
        repository.save(luggage);
        log.info("行李已標記為刪除狀態：id={}", id);
        return true;
    }

    @Transactional
    public boolean retrieveLuggage(Long id, List<String> scannedLuggage, String idCardNumber) {
        log.info("處理行李取件：id={}，掃描標籤數量={}，身份證={}",
                id, scannedLuggage.size(), idCardNumber != null ? "已提供" : "未提供");

        Luggage luggage = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行李記錄不存在：ID=" + id));

        if (luggage.getStatus() == LuggageStatus.RETRIEVED) {
            throw new IllegalStateException("行李已完成取件，無需重複操作：ID=" + id);
        }

        log.debug("跳過行李數量匹配檢查（前端已驗證）：訂單記錄={}件，實際掃描={}件",
                Optional.ofNullable(luggage.getLuggageCount()).orElse(1), scannedLuggage.size());

        if (idCardNumber != null && !idCardNumber.trim().isEmpty()) {
            String trimmedIdCard = idCardNumber.trim();
            luggage.setIdNumber(trimmedIdCard);
            log.info("行李取件身份證記錄：id={}，身份證號={}", id, trimmedIdCard);
        }

        luggage.setStatus(LuggageStatus.RETRIEVED);
        luggage.setCheckoutTime(LocalDateTime.now());
        repository.save(luggage);

        log.info("行李取件成功：id={}，取件時間={}", id, luggage.getCheckoutTime().format(DATE_FORMATTER));
        return true;
    }

    // ====================== 核心列印功能（保持不变，备注在HTML模板中处理） ======================
    public boolean printTwoTickets(Long luggageId) {
        log.info("開始執行列印雙份標籤：行李ID={}", luggageId);
        WebDriver driver = null;
        tempHtmlFile = null;

        try {
            Luggage luggage = repository.findById(luggageId)
                    .orElseThrow(() -> new ResourceNotFoundException("未找到行李記錄：ID=" + luggageId));

            String activePrinter = printerManager.getActivePrinterName();
            if (!printerManager.preCheckBeforePrint(activePrinter)) {
                log.error("列印失敗：印表機預檢查不通過，行李ID={}", luggageId);
                return false;
            }

            PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrinter == null || !defaultPrinter.getName().contains("XP-80C1")) {
                log.error("列印失敗：系統默認印表機不是XP-80C1，當前默認：{}",
                        defaultPrinter != null ? defaultPrinter.getName() : "無");
                return false;
            }

            String htmlContent = generateFrontendLikeHtml(luggage);
            tempHtmlFile = createTempHtmlFile(htmlContent);
            String tempFileUrl = tempHtmlFile.toURI().toString();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

            driver = new ChromeDriver(options);
            driver.get(tempFileUrl);

            // 顯式等待二維碼渲染完成（最多1.5秒）
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1500));
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ticket-qr")));
                log.debug("頁面加載完成，二維碼已渲染");
            } catch (org.openqa.selenium.TimeoutException e) {
                log.warn("頁面加載超時（1.5秒），嘗試繼續列印", e);
            }

            // 系統級按鍵模擬
            SystemKeySender.sendCombination(0x11, 0x50);
            Thread.sleep(100);
            SystemKeySender.keyDown(0x0D);
            SystemKeySender.keyUp(0x0D);
            Thread.sleep(100);

            markAsQrGenerated(luggageId);
            log.info("標籤列印完成：行李ID={}，已發送至{}", luggageId, defaultPrinter.getName());
            return true;

        } catch (Exception e) {
            log.error("列印失敗：行李ID={}", luggageId, e);
            return false;
        } finally {
            if (driver != null) {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                driver.quit();
            }
            if (tempHtmlFile != null && tempHtmlFile.exists()) {
                if (tempHtmlFile.delete()) {
                    log.debug("臨時HTML文件已刪除：{}", tempHtmlFile.getAbsolutePath());
                }
            }
        }
    }

    // ====================== 輔助方法（新增备注显示逻辑） ======================
    private String generateFrontendLikeHtml(Luggage luggage) throws IOException {
        String guestName = filterRareChars(luggage.getGuestName());
        String phoneLast4 = getPhoneLast4(luggage.getPhone());
        String roomNumber = filterRareChars(Optional.ofNullable(luggage.getRoomNumber()).orElse("未填寫"));
        String storageLocation = filterRareChars(Optional.ofNullable(luggage.getStorageLocation()).orElse("未指定"));
        String checkinTime = luggage.getCheckinTime().format(DATE_FORMATTER);
        String remark = Optional.ofNullable(luggage.getRemark()).orElse("無");
        if (remark.length() > 20) {
            remark = remark.substring(0, 20) + "...";
        }

        String logoBase64 = getImageAsBase64(HOTEL_LOGO_PATH);

        // 二維碼內容（JSON格式，保持不变）
        String customerQr = generateQrCodeBase64(buildQrJsonContent("customer_ticket", luggage.getId()));
        String luggageQr = generateQrCodeBase64(buildQrJsonContent("luggage_tag", luggage.getId()));

        // HTML模板：新增备注显示（客人凭证+行李标签都添加）
        return String.format("""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"></meta>
    <title>行李標籤列印</title>
    <style>
        @page { size: %dmm auto; margin: 5mm; }
        body { font-family: Arial, 'Microsoft JhengHei', 'SimSun', sans-serif; margin: 0; padding: 0; }
        .ticket { page-break-after: always; padding: 10px; text-align: center; width: %dmm; box-sizing: border-box; }
        .ticket:last-child { page-break-after: auto; }
        .hotel-logo { max-width: 60px; margin-bottom: 5px; object-fit: contain; }
        .ticket-header { font-weight: bold; font-size: 16px; margin-bottom: 8px; }
        .divider { border-bottom: 1px solid #000; margin: 3px 0 8px; }
        .ticket-info { font-size: 20px; margin: 4px 0; text-align: left; padding-left: 10px; }
        .ticket-info.large-print { font-size: 25pt; font-weight: bold; margin: 10px 0; text-align: left; word-break: break-all; }
        .field-label { color: #222; min-width: 90px; display: inline-block; font-weight: bold; }
        .ticket-qr { width: 180px; height: 180px; margin: 8px auto; }
        .terms { font-size: 13px; text-align: left; line-height: 1.2; padding: 0 5px; margin-top: 10px; }
    </style>
</head>
<body>
    <!-- 第一張：客人取件憑證（新增备注行） -->
    <div class="ticket">
        <div class="ticket-header">
            <img src="data:image/jpeg;base64,%s" class="hotel-logo"></img>
            <div>行李寄存憑證</div>
        </div>
        <div class="divider"></div>
        <div class="ticket-info large-print">
            <span class="field-label">行李ID:</span> <span>%d</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">客人姓名:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">手機尾號:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">房間號:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">存放區域:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">行李件數:</span> <span>%d</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">行李備註:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">寄存時間:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">客戶簽名:</span>
            <span style="border-bottom: 1px solid #333; width: 180px; display: inline-block; margin-left: 10px;"></span>
        </div>
        <img src="data:image/png;base64,%s" alt="取件二維碼" class="ticket-qr"></img>
        <div class="terms">
            <div><strong>服務條款:</strong></div>
            <div>1.本酒店可代客人免費存放行李在貯物室，為期不超過三天，如超過限期，本酒店將按日徵收每件澳門幣貳拾圓正作為貯存費，直至領回為止。</div>
            <div>2.本酒店在任何情況下不負責行李之損壞或遺失。</div>
            <div>3.所有寄存物件，如在十五天內無人認領，本酒店有權處理該物品且無須承擔任何賠償責任。</div>
            <div>4.使用此服務即表示已閱讀並同意上述條款。</div>
            <div>1.This is our policy to keep Hotel guest's luggage in our storeroom free of charge for no more than three days. We will levy a storage charge of MOP20.00 per day on each item thereafter until they are being claimed.</div>
            <div>2.The Hotel will not be held responsible for any loss or damage of the luggage deposited or articles contained in them.</div>
            <div>3.The Hotel reserves the right to dispose any unclaimed luggage held in our storage for more than FIFTEEN DAYS without any liability.</div>
            <div>4.By using this service, guests acknowledge and agree to these terms.</div>
        </div>
    </div>

    <!-- 第二張：行李存放標籤（新增备注行） -->
    <div class="ticket">
        <div class="ticket-header">
            <img src="data:image/jpeg;base64,%s" class="hotel-logo"></img>
            <div>行李存放標籤</div>
        </div>
        <div class="divider"></div>
        <div class="ticket-info large-print">
            <span class="field-label">行李ID:</span> <span>%d</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">客人姓名:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">房間號:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">存放區域:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">行李件數:</span> <span>%d &nbsp;&nbsp;</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">行李備註:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">寄存時間:</span> <span>%s</span>
        </div>
        <div class="ticket-info">
            <span class="field-label">客戶簽名:</span>
            <span style="border-bottom: 1px solid #333; width: 180px; display: inline-block; margin-left: 10px;"></span>
        </div>
        <img src="data:image/png;base64,%s" alt="行李二維碼" class="ticket-qr"></img>
    </div>
</body>
</html>
""",
                LABEL_WIDTH_MM, LABEL_WIDTH_MM,
                logoBase64,
                luggage.getId(), guestName, phoneLast4, roomNumber, storageLocation,
                luggage.getLuggageCount(), remark, checkinTime, customerQr,
                logoBase64,
                luggage.getId(), guestName, roomNumber, storageLocation,
                luggage.getLuggageCount(), remark, checkinTime, luggageQr
        );
    }

    // 以下辅助方法保持不变
    private String buildQrJsonContent(String type, Long luggageId) {
        try {
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("type", type); // 類型：customer_ticket 或 luggage_tag
            qrData.put("luggageId", luggageId); // 行李ID
            return OBJECT_MAPPER.writeValueAsString(qrData); // 生成標準JSON
        } catch (JsonProcessingException e) {
            log.error("生成二維碼JSON內容失敗：type={}，luggageId={}", type, luggageId, e);
            return "LUGGAGE-" + type + "-" + luggageId; // 異常時返回兼容格式
        }
    }

    private String getPhoneLast4(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "未填寫";
        }
        String cleanedPhone = phone.trim().replaceAll("\\D", "");
        return cleanedPhone.length() >= 4 ? cleanedPhone.substring(cleanedPhone.length() - 4) : cleanedPhone;
    }

    private String filterRareChars(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "未填寫";
        }
        return text.trim()
                .replace("鬱", "郁")
                .replace("龜", "龜")
                .replace("灣", "灣")
                .replace("𠕁", "加")
                .replace("𨐫", "網")
                .replace("裡", "裏")
                .replace("衆", "眾");
    }

    private String getImageAsBase64(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("圖像資源不存在：" + resourcePath);
            }
            byte[] imageBytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("二維碼生成失敗：{}", content, e);
            return "";
        }
    }

    private File createTempHtmlFile(String htmlContent) throws IOException {
        File tempFile = File.createTempFile("luggage_label_", ".html");
        try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
        log.debug("創建臨時HTML文件：{}", tempFile.getAbsolutePath());
        return tempFile;
    }

}