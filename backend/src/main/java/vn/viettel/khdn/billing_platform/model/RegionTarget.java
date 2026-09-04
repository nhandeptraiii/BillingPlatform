package vn.viettel.khdn.billing_platform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "region_targets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"region_id", "billing_period_id"})
})
public class RegionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_period_id", nullable = false)
    private BillingPeriod billingPeriod;

    @Column(name = "target_customer_percent")
    private Double targetCustomerPercent; // Ví dụ: 99.6 (tương ứng 99.6%)

    @Column(name = "target_revenue_percent")
    private Double targetRevenuePercent; // Ví dụ: 99.8 (tương ứng 99.8%)

    @Column(name = "target_ftth_n1_percent")
    private Double targetFtthN1Percent; // Ví dụ: 95.0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
