package vn.viettel.khdn.billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.viettel.khdn.billing_platform.model.dto.dashboard.ResConsultantPerformanceDTO;
import vn.viettel.khdn.billing_platform.model.dto.dashboard.ResDashboardOverviewDTO;
import vn.viettel.khdn.billing_platform.model.dto.dashboard.ResConsultantDailyStatsDTO;
import vn.viettel.khdn.billing_platform.repository.CustomerBillingRecordRepository;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.enums.DebtStatusEnum;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Font;

import vn.viettel.khdn.billing_platform.repository.RegionTargetRepository;
import vn.viettel.khdn.billing_platform.model.RegionTarget;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CustomerBillingRecordRepository repository;
    private final RegionTargetRepository regionTargetRepository;

    public ResDashboardOverviewDTO getDashboardOverview(Long periodId, User currentUser) {
        List<Object[]> stats;
        List<Object[]> ftthN1Stats;
        if (currentUser.getRole() == RoleEnum.MANAGER
                || currentUser.getRole() == RoleEnum.ADMIN) {
            Long regionId = currentUser.getRole() == RoleEnum.ADMIN ? null
                    : (currentUser.getRegion() != null ? currentUser.getRegion().getId() : null);
            stats = repository.getProgressByPeriod(periodId, regionId);
            ftthN1Stats = repository.getFtthN1Stats(periodId, regionId);
        } else if (currentUser.getRole() == RoleEnum.NVKD) {
            stats = repository.getProgressByPeriodAndManager(periodId, currentUser.getId());
            ftthN1Stats = repository.getFtthN1StatsByManager(periodId, currentUser.getId());
        } else {
            stats = repository.getProgressByPeriodAndConsultant(periodId, currentUser.getId());
            ftthN1Stats = repository.getFtthN1StatsByConsultant(periodId, currentUser.getId());
        }

        long totalRecords = 0L;
        long collectedRecords = 0L;
        long markedDebtRecords = 0L;
        BigDecimal expectedAmount = BigDecimal.ZERO;
        BigDecimal collectedAmount = BigDecimal.ZERO;

        for (Object[] row : stats) {
            DebtStatusEnum debtStatus = (DebtStatusEnum) row[1];
            Long count = ((Number) row[2]).longValue();
            BigDecimal amtDue = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            BigDecimal colAmt = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;

            totalRecords += count;
            expectedAmount = expectedAmount.add(amtDue);

            // Số lượng hồ sơ đã thu (totalCollectedRecords) & gạch nợ: Căn cứ vào gạch nợ
            if (DebtStatusEnum.DA_GACH_NO == debtStatus) {
                collectedRecords += count;
                markedDebtRecords += count;
            }

            // Số tiền đã thu (totalCollectedAmount): Được tính chính xác từ câu query theo quy tắc:
            // Luôn lấy TOÀN BỘ số tiền thực thu, không giới hạn (không cắt bỏ phần đóng dư). Khớp 100% với file Excel.
            collectedAmount = collectedAmount.add(colAmt);
        }

        double amountProgressPercentage = 0.0;
        double recordsProgressPercentage = 0.0;

        if (expectedAmount.compareTo(BigDecimal.ZERO) > 0) {
            amountProgressPercentage = collectedAmount.divide(expectedAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        if (totalRecords > 0) {
            recordsProgressPercentage = (double) collectedRecords / totalRecords * 100;
        }

        Double targetCustomerPercent = null;
        Double targetRevenuePercent = null;
        Double targetFtthN1Percent = null;

        Long regionIdForTarget = currentUser.getRole() == RoleEnum.ADMIN ? null
                : (currentUser.getRegion() != null ? currentUser.getRegion().getId() : null);
        if (regionIdForTarget != null) {
            Optional<RegionTarget> optTarget = regionTargetRepository
                    .findByRegionIdAndBillingPeriodId(regionIdForTarget, periodId);
            if (optTarget.isPresent()) {
                targetCustomerPercent = optTarget.get().getTargetCustomerPercent();
                targetRevenuePercent = optTarget.get().getTargetRevenuePercent();
                targetFtthN1Percent = optTarget.get().getTargetFtthN1Percent();
            }
        }

        Long ftthN1TotalRecords = 0L;
        BigDecimal ftthN1ExpectedAmount = BigDecimal.ZERO;
        Long ftthN1CollectedRecords = 0L;
        BigDecimal ftthN1CollectedAmount = BigDecimal.ZERO;
        
        if (ftthN1Stats != null && !ftthN1Stats.isEmpty()) {
            Object[] fRow = ftthN1Stats.get(0);
            if (fRow[0] != null) ftthN1TotalRecords = ((Number) fRow[0]).longValue();
            if (fRow[1] != null) ftthN1ExpectedAmount = new BigDecimal(fRow[1].toString());
            if (fRow[2] != null) ftthN1CollectedRecords = ((Number) fRow[2]).longValue();
            if (fRow[3] != null) ftthN1CollectedAmount = new BigDecimal(fRow[3].toString());
        }

        return new ResDashboardOverviewDTO(
                totalRecords,
                collectedRecords,
                markedDebtRecords,
                expectedAmount,
                collectedAmount,
                amountProgressPercentage,
                recordsProgressPercentage,
                targetCustomerPercent,
                targetRevenuePercent,
                targetFtthN1Percent,
                ftthN1TotalRecords,
                ftthN1ExpectedAmount,
                ftthN1CollectedRecords,
                ftthN1CollectedAmount);
    }

    public List<ResConsultantPerformanceDTO> getConsultantPerformance(Long periodId, User currentUser) {
        Long regionId = null;
        Long managerId = null;
        boolean includeUnassigned = false;

        if (currentUser.getRole() == RoleEnum.MANAGER || currentUser.getRole() == RoleEnum.ADMIN) {
            regionId = currentUser.getRole() == RoleEnum.ADMIN ? null
                    : (currentUser.getRegion() != null ? currentUser.getRegion().getId() : null);
            includeUnassigned = true;
        } else if (currentUser.getRole() == RoleEnum.NVKD) {
            managerId = currentUser.getId();
        } else {
            return new ArrayList<>(); // CONSULTANT không dùng API này
        }

        // Chỉ tiêu của khu vực (ADMIN không thuộc khu vực nào => chưa đặt chỉ tiêu)
        RegionTarget target = null;
        if (currentUser.getRole() != RoleEnum.ADMIN && currentUser.getRegion() != null) {
            target = regionTargetRepository
                    .findByRegionIdAndBillingPeriodId(currentUser.getRegion().getId(), periodId)
                    .orElse(null);
        }
        Double customerPct = target != null ? target.getTargetCustomerPercent() : null;
        Double revenuePct = target != null ? target.getTargetRevenuePercent() : null;
        Double ftthPct = target != null ? target.getTargetFtthN1Percent() : null;

        List<ResConsultantPerformanceDTO> result = new ArrayList<>();

        for (Object[] row : repository.getConsultantStats(periodId, regionId, managerId)) {
            Long consultantId = ((Number) row[0]).longValue();
            RoleEnum rowRole = (RoleEnum) row[2];
            // NVKD được giao hồ sơ trực tiếp: tự làm nhóm của chính mình
            Long mId = rowRole == RoleEnum.NVKD ? consultantId : (row[3] != null ? ((Number) row[3]).longValue() : null);
            String mName = rowRole == RoleEnum.NVKD ? (String) row[1] : (String) row[4];

            result.add(buildPerformance(consultantId, (String) row[1], mId, mName,
                    toLong(row[5]), toBigDecimal(row[6]), toLong(row[7]), toBigDecimal(row[8]),
                    toLong(row[9]), toBigDecimal(row[10]), toLong(row[11]), toBigDecimal(row[12]),
                    customerPct, revenuePct, ftthPct));
        }

        if (includeUnassigned) {
            for (Object[] row : repository.getUnassignedStats(periodId, regionId)) {
                long total = toLong(row[0]);
                if (total > 0) {
                    result.add(buildPerformance(null, "Chưa gán tư vấn viên", null, null,
                            total, toBigDecimal(row[1]), toLong(row[2]), toBigDecimal(row[3]),
                            0L, BigDecimal.ZERO, 0L, BigDecimal.ZERO,
                            customerPct, revenuePct, ftthPct));
                }
            }
        }

        // Gom nhóm theo NVKD (nhóm chưa gán NVKD xếp cuối), trong nhóm xếp theo tên
        result.sort(Comparator
                .comparing((ResConsultantPerformanceDTO d) -> d.getManagerName() == null ? 1 : 0)
                .thenComparing(d -> d.getManagerName() == null ? "" : d.getManagerName(),
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(d -> d.getConsultantId() == null ? 1 : 0)
                .thenComparing(d -> d.getConsultantName() == null ? "" : d.getConsultantName(),
                        String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private ResConsultantPerformanceDTO buildPerformance(Long consultantId, String consultantName,
            Long managerId, String managerName,
            long totalRecords, BigDecimal totalAmount, long collectedRecords, BigDecimal collectedAmount,
            long ftthTotal, BigDecimal ftthAmount, long ftthCollected, BigDecimal ftthCollectedAmount,
            Double customerPct, Double revenuePct, Double ftthPct) {
        ResConsultantPerformanceDTO dto = new ResConsultantPerformanceDTO();
        dto.setConsultantId(consultantId);
        dto.setConsultantName(consultantName);
        dto.setManagerId(managerId);
        dto.setManagerName(managerName);
        dto.setTargetRecords(totalRecords);
        dto.setTargetAmount(totalAmount);
        dto.setCollectedRecords(collectedRecords);
        dto.setCollectedAmount(collectedAmount);
        dto.setTargetCustomerPercent(customerPct);
        dto.setTargetRevenuePercent(revenuePct);
        if (customerPct != null) {
            long required = requiredRecords(totalRecords, customerPct);
            dto.setRequiredRecords(required);
            dto.setRemainingRecords(Math.max(required - collectedRecords, 0L));
        }
        if (revenuePct != null) {
            BigDecimal required = requiredAmount(totalAmount, revenuePct);
            dto.setRequiredAmount(required);
            dto.setRemainingAmount(required.subtract(collectedAmount).max(BigDecimal.ZERO));
        }

        dto.setFtthN1TotalRecords(ftthTotal);
        dto.setFtthN1ExpectedAmount(ftthAmount);
        dto.setFtthN1CollectedRecords(ftthCollected);
        dto.setFtthN1CollectedAmount(ftthCollectedAmount);
        dto.setTargetFtthN1Percent(ftthPct);
        if (ftthPct != null) {
            long requiredRecords = requiredRecords(ftthTotal, ftthPct);
            BigDecimal requiredAmount = requiredAmount(ftthAmount, ftthPct);
            dto.setFtthN1RequiredRecords(requiredRecords);
            dto.setFtthN1RequiredAmount(requiredAmount);
            dto.setFtthN1RemainingRecords(Math.max(requiredRecords - ftthCollected, 0L));
            dto.setFtthN1RemainingAmount(requiredAmount.subtract(ftthCollectedAmount).max(BigDecimal.ZERO));
        }
        return dto;
    }

    /** Số KH cần đạt = làm tròn LÊN (đầu kỳ x %). */
    private static long requiredRecords(long total, double percent) {
        return BigDecimal.valueOf(total).multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING).longValue();
    }

    /** Số tiền cần đạt = làm tròn LÊN đến đồng (đầu kỳ x %). */
    private static BigDecimal requiredAmount(BigDecimal total, double percent) {
        return total.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING);
    }

    private static long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private static BigDecimal toBigDecimal(Object value) {
        return value != null ? new BigDecimal(value.toString()) : BigDecimal.ZERO;
    }

    public List<ResConsultantDailyStatsDTO> getDailyStats(LocalDate date, User currentUser) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Object[]> data;
        if (currentUser.getRole() == RoleEnum.MANAGER || currentUser.getRole() == RoleEnum.ADMIN) {
            Long regionId = currentUser.getRole() == RoleEnum.ADMIN ? null
                    : (currentUser.getRegion() != null ? currentUser.getRegion().getId() : null);
            data = repository.getConsultantDailyStats(startOfDay, endOfDay, regionId);
        } else if (currentUser.getRole() == RoleEnum.NVKD) {
            data = repository.getConsultantDailyStatsByManager(startOfDay, endOfDay, currentUser.getId());
        } else {
            return new ArrayList<>();
        }
        List<ResConsultantDailyStatsDTO> result = new ArrayList<>();

        for (Object[] row : data) {
            Long consultantId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String consultantName = (String) row[1];
            Instant firstBillPrintedAt = (Instant) row[2];
            Long collectedCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            result.add(new ResConsultantDailyStatsDTO(
                    consultantId,
                    consultantName,
                    firstBillPrintedAt,
                    collectedCount));
        }
        return result;
    }

    public byte[] exportConsultantPerformance(Long periodId, User currentUser) {
        List<ResConsultantPerformanceDTO> records = getConsultantPerformance(periodId, currentUser);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try (workbook; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo tiến độ");
            if (sheet instanceof org.apache.poi.xssf.streaming.SXSSFSheet) {
                ((org.apache.poi.xssf.streaming.SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            }

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Row 0: nhóm cột
            Row row0 = sheet.createRow(0);
            row0.setHeight((short) 500);
            String[][] groups = { { "Đầu kỳ", "3", "4" }, { "Đã thu (đã gạch nợ)", "5", "8" },
                    { "Phải thu để đạt chỉ tiêu", "9", "10" } };
            for (String[] g : groups) {
                int from = Integer.parseInt(g[1]);
                int to = Integer.parseInt(g[2]);
                org.apache.poi.ss.usermodel.Cell c = row0.createCell(from);
                c.setCellValue(g[0]);
                c.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, from, to));
            }

            // Row 1: tiêu đề cột
            Row row1 = sheet.createRow(1);
            String[] headers = {
                    "STT", "NVKD quản lý", "Tư vấn viên",
                    "Khách hàng", "Số tiền",
                    "Khách hàng", "% KH", "Số tiền", "% Tiền",
                    "Khách hàng", "Số tiền"
            };
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = row1.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 2;
            int stt = 1;
            for (ResConsultantPerformanceDTO r : records) {
                Row row = sheet.createRow(rowIdx++);
                BigDecimal totalAmount = r.getTargetAmount() != null ? r.getTargetAmount() : BigDecimal.ZERO;
                BigDecimal collectedAmount = r.getCollectedAmount() != null ? r.getCollectedAmount() : BigDecimal.ZERO;
                long totalRecords = r.getTargetRecords() != null ? r.getTargetRecords() : 0L;
                long collectedRecords = r.getCollectedRecords() != null ? r.getCollectedRecords() : 0L;

                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(r.getManagerName() != null ? r.getManagerName() : "");
                row.createCell(2).setCellValue(r.getConsultantName() != null ? r.getConsultantName() : "");
                row.createCell(3).setCellValue(totalRecords);
                row.createCell(4).setCellValue(totalAmount.doubleValue());
                row.createCell(5).setCellValue(collectedRecords);
                row.createCell(6).setCellValue(percentText(collectedRecords, totalRecords));
                row.createCell(7).setCellValue(collectedAmount.doubleValue());
                row.createCell(8).setCellValue(percentText(collectedAmount.doubleValue(), totalAmount.doubleValue()));
                if (r.getRemainingRecords() != null && r.getRemainingAmount() != null) {
                    row.createCell(9).setCellValue(r.getRemainingRecords());
                    row.createCell(10).setCellValue(r.getRemainingAmount().doubleValue());
                } else {
                    row.createCell(9).setCellValue("Chưa đặt chỉ tiêu");
                    row.createCell(10).setCellValue("Chưa đặt chỉ tiêu");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo file Excel: " + e.getMessage(), e);
        } finally {
            workbook.dispose();
        }
    }

    private static String percentText(double part, double total) {
        return total > 0 ? String.format("%.2f %%", part / total * 100) : "0.00 %";
    }
}
