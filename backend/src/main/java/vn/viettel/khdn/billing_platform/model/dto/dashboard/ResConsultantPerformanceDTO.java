package vn.viettel.khdn.billing_platform.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Thống kê theo từng tư vấn viên, kèm thông tin NVKD quản lý để frontend gom nhóm.
 * <p>
 * - targetRecords/targetAmount: GIỮ TÊN CŨ cho tương thích, thực chất là tổng ĐẦU KỲ.
 * - collectedRecords/collectedAmount: chỉ tính hồ sơ ĐÃ GẠCH NỢ (tiền cắt trần ở số phải thu).
 * - required*: số cần đạt = đầu kỳ x % chỉ tiêu khu vực; null nếu khu vực CHƯA ĐẶT chỉ tiêu.
 * - remaining*: còn phải thu để đạt chỉ tiêu = max(required - collected, 0); null nếu chưa đặt chỉ tiêu.
 * - Dòng "Chưa gán tư vấn viên": consultantId = null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResConsultantPerformanceDTO {
    private Long consultantId;
    private String consultantName;

    // NVKD quản lý (null = trực thuộc quản lý khu vực / chưa gán nhóm)
    private Long managerId;
    private String managerName;

    private Long targetRecords;
    private BigDecimal targetAmount;
    private Long collectedRecords;
    private BigDecimal collectedAmount;

    // Chỉ tiêu khu vực (null = chưa đặt)
    private Double targetCustomerPercent;
    private Double targetRevenuePercent;
    private Long requiredRecords;
    private BigDecimal requiredAmount;
    private Long remainingRecords;
    private BigDecimal remainingAmount;

    // FTTH N1
    private Long ftthN1TotalRecords;
    private BigDecimal ftthN1ExpectedAmount;
    private Long ftthN1CollectedRecords;
    private BigDecimal ftthN1CollectedAmount;
    private Double targetFtthN1Percent;
    private Long ftthN1RequiredRecords;
    private BigDecimal ftthN1RequiredAmount;
    private Long ftthN1RemainingRecords;
    private BigDecimal ftthN1RemainingAmount;
}
