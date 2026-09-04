package vn.viettel.khdn.billing_platform.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResDashboardOverviewDTO {
    private Long totalRecordsImported;
    private Long totalCollectedRecords;
    private Long totalMarkedDebtRecords;
    private BigDecimal totalExpectedAmount;
    private BigDecimal totalCollectedAmount;
    private Double amountProgressPercentage;
    private Double recordsProgressPercentage;
    private Double targetCustomerPercent;
    private Double targetRevenuePercent;
    
    // Chỉ tiêu FTTH N1
    private Long ftthN1TotalRecords;
    private BigDecimal ftthN1ExpectedAmount;
    private Long ftthN1CollectedRecords;
    private BigDecimal ftthN1CollectedAmount;
}
