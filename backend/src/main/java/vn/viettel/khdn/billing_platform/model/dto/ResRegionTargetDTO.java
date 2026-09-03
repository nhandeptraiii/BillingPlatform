package vn.viettel.khdn.billing_platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.viettel.khdn.billing_platform.model.RegionTarget;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRegionTargetDTO {
    private Long id;
    private Long regionId;
    private Long billingPeriodId;
    private Double targetCustomerPercent;
    private Double targetRevenuePercent;

    public ResRegionTargetDTO(RegionTarget target) {
        this.id = target.getId();
        this.regionId = target.getRegion() != null ? target.getRegion().getId() : null;
        this.billingPeriodId = target.getBillingPeriod() != null ? target.getBillingPeriod().getId() : null;
        this.targetCustomerPercent = target.getTargetCustomerPercent();
        this.targetRevenuePercent = target.getTargetRevenuePercent();
    }
}
