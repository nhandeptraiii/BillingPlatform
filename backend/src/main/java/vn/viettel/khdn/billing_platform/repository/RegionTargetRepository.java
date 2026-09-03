package vn.viettel.khdn.billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.viettel.khdn.billing_platform.model.RegionTarget;

import java.util.Optional;

@Repository
public interface RegionTargetRepository extends JpaRepository<RegionTarget, Long> {
    Optional<RegionTarget> findByRegionIdAndBillingPeriodId(Long regionId, Long billingPeriodId);
}
