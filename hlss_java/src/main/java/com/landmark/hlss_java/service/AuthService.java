package com.landmark.hlss_java.service;

import com.landmark.hlss_java.dto.AuthRequest;
import com.landmark.hlss_java.dto.AuthResponse;
import com.landmark.hlss_java.entity.Staff;
import com.landmark.hlss_java.exception.AuthException;
import com.landmark.hlss_java.repository.StaffRepository;
import com.landmark.hlss_java.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final StaffRepository staffRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse authenticate(AuthRequest request) {
        // 1. 增加輸入驗證
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new AuthException("用戶名不能為空");
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new AuthException("密碼不能為空");
        }

        // 2. 查找用戶（提供更明確的錯誤訊息）
        Staff staff = staffRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("用戶名不存在或輸入錯誤"));

        // 3. 密碼驗證（開發階段可加入除錯日誌）
        log.debug("輸入密碼: " + request.password()); // 正式環境應移除
        log.debug("數據庫存儲的密碼哈希: " + staff.getPassword());

        if (!passwordEncoder.matches(request.password(), staff.getPassword())) {
            System.out.println("輸入的密碼: " + request.password());
            System.out.println("資料庫中的雜湊: " + staff.getPassword());
            System.out.println("符合結果: " + passwordEncoder.matches(request.password(), staff.getPassword()));
            throw new AuthException("密碼不正確");
        }

        // 4. 生成JWT令牌
        String token = tokenProvider.generateToken(staff);
        return new AuthResponse(
                token,
                Instant.now().plus(7, ChronoUnit.DAYS), // 7天有效期
                staff.getName(),
                staff.getRole().name()
        );
    }

    public void logout() {
        // 可實作Token黑名單機制
        log.info("用戶登出成功");
    }
}