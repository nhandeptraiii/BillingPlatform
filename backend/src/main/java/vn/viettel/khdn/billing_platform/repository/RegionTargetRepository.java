package vn.viettel.khdn.billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.viettel.khdn.billing_platform.model.RegionTarget;

import java.util.Optional;

@Repository
public interface RegionTargetRepository extends JpaRepository<RegionTarget, Long> {
    Optional<RegionTarget> findByRegionIdAndBillingPeriodId(Long regionId, Long billingPeriodId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RegionTarget t WHERE t.billingPeriod.id = :periodId AND t.region.id = :regionId")
    int deleteByBillingPeriodIdAndRegionId(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RegionTarget t WHERE t.billingPeriod.id = :periodId")
    int deleteAllByBillingPeriodId(@Param("periodId") Long periodId);

    boolean existsByUpdatedById(Long userId);

    boolean existsByBillingPeriodId(Long periodId);
}

