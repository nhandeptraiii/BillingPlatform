package vn.viettel.khdn.billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.dto.dashboard.ResConsultantPerformanceDTO;
import vn.viettel.khdn.billing_platform.repository.BillingPeriodRepository;
import vn.viettel.khdn.billing_platform.repository.UserRepository;
import vn.viettel.khdn.billing_platform.service.DashboardService;
import vn.viettel.khdn.billing_platform.util.SecurityUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * Controller alias hỗ trợ các request gọi đến /consultant hoặc /consultants
 * (Ví dụ: /consultant?month=9&year=2026 hoặc /consultant?month=9year=2026)
 */
@RestController
@RequestMapping({ "/consultant", "/consultants-stats" })
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ConsultantController {

    private final DashboardService dashboardService;
    private final BillingPeriodRepository periodRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new EntityNotFoundException("Chưa đăng nhập"));
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
    }

    private Long resolvePeriodId(String monthStr, Integer year) {
        Integer m = null;
        Integer y = year;

        if (monthStr != null && !monthStr.isBlank()) {
            String s = monthStr.trim().toLowerCase();
            if (s.contains("year=")) {
                String[] parts = s.split("year=");
                try {
                    m = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
                if (parts.length > 1 && y == null) {
                    try {
                        y = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {
                    }
                }
            } else {
                try {
                    m = Integer.parseInt(s.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
        }

        if (m == null || y == null) {
            LocalDate now = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            if (m == null)
                m = now.getMonthValue();
            if (y == null)
                y = now.getYear();
        }

        return periodRepository.findByMonthAndYear(m, y)
                .map(vn.viettel.khdn.billing_platform.model.BillingPeriod::getId)
                .orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'NVKD')")
    public ResponseEntity<List<ResConsultantPerformanceDTO>> getConsultantPerformance(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) Integer year) {
        Long periodId = resolvePeriodId(month, year);
        if (periodId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(dashboardService.getConsultantPerformance(periodId, getCurrentUser()));
    }
}
