package vn.viettel.khdn.billing_platform.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class ReqRegionTargetDTO {
    
    @NotNull(message = "billingPeriodId không được để trống")
    private Long billingPeriodId;
    
    private Long regionId; // Dành cho ADMIN, nếu null thì lấy region của user hiện tại
    
    @NotNull(message = "Tỷ lệ số lượng KH mục tiêu không được để trống")
    @Min(value = 0, message = "Tỷ lệ phải từ 0 trở lên")
    @Max(value = 100, message = "Tỷ lệ không được vượt quá 100")
    private Double targetCustomerPercent;
    
    @NotNull(message = "Tỷ lệ doanh thu mục tiêu không được để trống")
    @Min(value = 0, message = "Tỷ lệ phải từ 0 trở lên")
    @Max(value = 100, message = "Tỷ lệ không được vượt quá 100")
    private Double targetRevenuePercent;
}
