package vn.viettel.khdn.billing_platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.viettel.khdn.billing_platform.model.CustomerBillingRecord;
import vn.viettel.khdn.billing_platform.model.enums.CollectionStatusEnum;
import vn.viettel.khdn.billing_platform.model.enums.DebtStatusEnum;
import vn.viettel.khdn.billing_platform.model.enums.SyncWarningEnum;

public interface CustomerBillingRecordRepository extends JpaRepository<CustomerBillingRecord, Long> {

    // SET NULL assigned_consultant khi xĂ³a user (trĂ¡nh FK violation)
    @Modifying
    @Query("UPDATE CustomerBillingRecord r SET r.assignedConsultant = NULL WHERE r.assignedConsultant.id = :consultantId")
    void clearAssignedConsultant(@Param("consultantId") Long consultantId);

    // Kiá»ƒm tra Ä‘Ă£ cĂ³ records trong ká»³ + khu vá»±c (Ä‘á»ƒ cháº·n re-import)
    boolean existsByBillingPeriodIdAndRegionId(Long billingPeriodId, Long regionId);

    // XĂ³a toĂ n bá»™ records cá»§a 1 ká»³ (dĂ¹ng khi xĂ³a Ä‘áº§u ká»³ â€” trÆ°á»›c khi xĂ³a BillingPeriod)
    @Modifying
    @Query("DELETE FROM CustomerBillingRecord r WHERE r.billingPeriod.id = :periodId")
    void deleteAllByBillingPeriodId(@Param("periodId") Long periodId);


    // TĂ¬m theo mĂ£ KH + ká»³ (dĂ¹ng khi import Ä‘á»‘i chiáº¿u)
    Optional<CustomerBillingRecord> findByCustomerCodeAndBillingPeriodId(
            String customerCode, Long billingPeriodId);

    // TĂ¬m theo sá»‘ TB + ká»³ (backup key khi import Ä‘á»‘i chiáº¿u)
    Optional<CustomerBillingRecord> findBySubscriberNumberAndBillingPeriodId(
            String subscriberNumber, Long billingPeriodId);

    // Chunked IN query: láº¥y records theo batch MĂ£ KH â€” trĂ¡nh N+1 mĂ  khĂ´ng OOM
    // Gá»i theo tá»«ng batch 500 mĂ£, khĂ´ng load toĂ n bá»™ vĂ o RAM má»™t láº§n
    List<CustomerBillingRecord> findAllByCustomerCodeInAndBillingPeriodId(
            Collection<String> customerCodes, Long billingPeriodId);

    // Bulk load toĂ n bá»™ records cá»§a 1 ká»³ (dĂ¹ng khi import Ä‘á»‘i chiáº¿u â€” trĂ¡nh N+1 query)
    // 1 cĂ¢u SELECT thay vĂ¬ N cĂ¢u, sau Ä‘Ă³ group trong memory
    List<CustomerBillingRecord> findAllByBillingPeriodId(Long billingPeriodId);

    // Scheduler cuá»‘i ngĂ y: tĂ¬m báº£n ghi DA_THANH_TOAN nhÆ°ng chÆ°a gáº¡ch ná»£
    List<CustomerBillingRecord> findByBillingPeriodIdAndCollectionStatusAndDebtStatus(
            Long periodId, CollectionStatusEnum collectionStatus, DebtStatusEnum debtStatus);

    // Danh sĂ¡ch cáº£nh bĂ¡o Ä‘á»“ng bá»™ (TH import Ä‘á»‘i chiáº¿u)
    List<CustomerBillingRecord> findByBillingPeriodIdAndSyncWarning(
            Long periodId, SyncWarningEnum syncWarning);

    // Cáº£nh bĂ¡o: DA_THANH_TOAN chÆ°a gáº¡ch ná»£ + INCONSISTENT (dĂ¹ng cho warnings API)
    @Query("""
        SELECT r FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId
          AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
          AND (
            (r.collectionStatus = 'DA_THANH_TOAN' AND r.debtStatus = 'CHUA_GACH_NO')
            OR r.syncWarning = 'INCONSISTENT'
            OR r.syncWarning = 'COLLECTED_NOT_MARKED'
          )
        """)
    Page<CustomerBillingRecord> findWarningsByPeriod(
            @Param("periodId") Long periodId,
            @Param("regionId") Long regionId,
            Pageable pageable);

    // Cáº£nh bĂ¡o dĂ nh cho NVKD (lá»c theo nhĂ³m quáº£n lĂ½)
    @Query("""
        SELECT r FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId
          AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
          AND (
            (r.collectionStatus = 'DA_THANH_TOAN' AND r.debtStatus = 'CHUA_GACH_NO')
            OR r.syncWarning = 'INCONSISTENT'
            OR r.syncWarning = 'COLLECTED_NOT_MARKED'
          )
        """)
    Page<CustomerBillingRecord> findWarningsByPeriodAndManager(
            @Param("periodId") Long periodId,
            @Param("managerId") Long managerId,
            Pageable pageable);


    // TĂ¬m kiáº¿m full-text + filter Ä‘a chiá»u (MANAGER xem táº¥t cáº£)
    @Query("""
        SELECT r FROM CustomerBillingRecord r
        LEFT JOIN r.assignedConsultant c
        WHERE (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:assignedUserId IS NULL OR c.id = :assignedUserId)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<CustomerBillingRecord> searchAll(
            @Param("periodId") Long periodId,
            @Param("regionId") Long regionId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("assignedUserId") Long assignedUserId,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
        SELECT r.id FROM CustomerBillingRecord r
        LEFT JOIN r.assignedConsultant c
        WHERE (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:assignedUserId IS NULL OR c.id = :assignedUserId)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    List<Long> findAllIdsAll(
            @Param("periodId") Long periodId,
            @Param("regionId") Long regionId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("assignedUserId") Long assignedUserId,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search);

    // CONSULTANT chá»‰ tháº¥y KH cá»§a mĂ¬nh
    @Query("""
        SELECT r FROM CustomerBillingRecord r
        WHERE r.assignedConsultant.id = :consultantId
          AND (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<CustomerBillingRecord> searchByConsultant(
            @Param("consultantId") Long consultantId,
            @Param("periodId") Long periodId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
        SELECT r.id FROM CustomerBillingRecord r
        WHERE r.assignedConsultant.id = :consultantId
          AND (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    List<Long> findAllIdsByConsultant(
            @Param("consultantId") Long consultantId,
            @Param("periodId") Long periodId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search);

    @Query("""
        SELECT r FROM CustomerBillingRecord r
        WHERE (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
          AND (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:assignedUserId IS NULL OR r.assignedConsultant.id = :assignedUserId)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<CustomerBillingRecord> searchByManager(
            @Param("managerId") Long managerId,
            @Param("periodId") Long periodId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("assignedUserId") Long assignedUserId,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
        SELECT r.id FROM CustomerBillingRecord r
        WHERE (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
          AND (:periodId IS NULL OR r.billingPeriod.id = :periodId)
          AND (:collectionStatus IS NULL OR r.collectionStatus = :collectionStatus)
          AND (:debtStatus IS NULL OR r.debtStatus = :debtStatus)
          AND (:assignedUserId IS NULL OR r.assignedConsultant.id = :assignedUserId)
          AND (:startOfDay IS NULL OR r.billPrintedAt >= :startOfDay)
          AND (:endOfDay IS NULL OR r.billPrintedAt < :endOfDay)
          AND (:subscriberNumber IS NULL OR r.subscriberNumber LIKE CONCAT('%', :subscriberNumber, '%'))
          AND (:customerName IS NULL OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:fullAddress IS NULL OR LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :fullAddress, '%')))
          AND (:search IS NULL OR
               LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               r.customerCode LIKE CONCAT('%', :search, '%') OR
               r.subscriberNumber LIKE CONCAT('%', :search, '%') OR
               r.phoneNumber LIKE CONCAT('%', :search, '%') OR
               LOWER(r.fullAddress) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    List<Long> findAllIdsByManager(
            @Param("managerId") Long managerId,
            @Param("periodId") Long periodId,
            @Param("collectionStatus") CollectionStatusEnum collectionStatus,
            @Param("debtStatus") DebtStatusEnum debtStatus,
            @Param("assignedUserId") Long assignedUserId,
            @Param("startOfDay") java.time.Instant startOfDay,
            @Param("endOfDay") java.time.Instant endOfDay,
            @Param("subscriberNumber") String subscriberNumber,
            @Param("customerName") String customerName,
            @Param("fullAddress") String fullAddress,
            @Param("search") String search);

    // Thá»‘ng kĂª tiáº¿n Ä‘á»™ theo ká»³
    @Query("""
        SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue),
               SUM(CASE 
                   WHEN r.collectedAmount IS NOT NULL THEN r.collectedAmount 
                   ELSE 0 
               END)
        FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId
          AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
        GROUP BY r.collectionStatus, r.debtStatus
        """)
    List<Object[]> getProgressByPeriod(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    // Thá»‘ng kĂª tiáº¿n Ä‘á»™ theo ká»³ vĂ  tÆ° váº¥n viĂªn
    @Query("""
        SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue),
               SUM(CASE 
                   WHEN r.collectedAmount IS NOT NULL THEN r.collectedAmount 
                   ELSE 0 
               END)
        FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId AND r.assignedConsultant.id = :consultantId
        GROUP BY r.collectionStatus, r.debtStatus
        """)
    List<Object[]> getProgressByPeriodAndConsultant(@Param("periodId") Long periodId, @Param("consultantId") Long consultantId);

    // Thá»‘ng kĂª theo tÆ° váº¥n viĂªn trong ká»³ (kĂ¨m chá»‰ tiĂªu) - Sá»‘ há»“ sÆ¡ vĂ  sá»‘ tiá»n Ä‘á»u cÄƒn cá»© vĂ o gáº¡ch ná»£
    @Query("""
        SELECT r.assignedConsultant.id, r.assignedConsultant.fullName,
               COUNT(r), SUM(r.amountDue),
               SUM(CASE WHEN r.debtStatus = 'DA_GACH_NO' THEN 1 ELSE 0 END),
               SUM(CASE 
                   WHEN r.collectedAmount IS NOT NULL THEN r.collectedAmount 
                   ELSE 0 
               END)
        FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId
          AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
        GROUP BY r.assignedConsultant.id, r.assignedConsultant.fullName
        """)
    List<Object[]> getConsultantPerformanceWithTarget(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    // Thá»‘ng kĂª giá» in bill Ä‘áº§u tiĂªn vĂ  sá»‘ lÆ°á»£ng thu trong ngĂ y cá»§a cĂ¡c tÆ° váº¥n viĂªn
    @Query("""
        SELECT r.assignedConsultant.id, r.assignedConsultant.fullName,
               MIN(r.billPrintedAt),
               COUNT(r)
        FROM CustomerBillingRecord r
        WHERE r.collectedAt >= :startOfDay AND r.collectedAt < :endOfDay
        AND r.collectionStatus = 'DA_THANH_TOAN'
        AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
        GROUP BY r.assignedConsultant.id, r.assignedConsultant.fullName
        """)
    List<Object[]> getConsultantDailyStats(@Param("startOfDay") java.time.Instant startOfDay, @Param("endOfDay") java.time.Instant endOfDay, @Param("regionId") Long regionId);

    // DĂ nh cho NVKD quáº£n lĂ½
    @Query("""
        SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue),
               SUM(CASE 
                   WHEN r.collectedAmount IS NOT NULL THEN r.collectedAmount 
                   ELSE 0 
               END)
        FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId 
          AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
        GROUP BY r.collectionStatus, r.debtStatus
        """)
    List<Object[]> getProgressByPeriodAndManager(@Param("periodId") Long periodId, @Param("managerId") Long managerId);

    @Query("""
        SELECT r.assignedConsultant.id, r.assignedConsultant.fullName,
               COUNT(r), SUM(r.amountDue),
               SUM(CASE WHEN r.debtStatus = 'DA_GACH_NO' THEN 1 ELSE 0 END),
               SUM(CASE 
                   WHEN r.collectedAmount IS NOT NULL THEN r.collectedAmount 
                   ELSE 0 
               END)
        FROM CustomerBillingRecord r
        WHERE r.billingPeriod.id = :periodId
          AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
        GROUP BY r.assignedConsultant.id, r.assignedConsultant.fullName
        """)
    List<Object[]> getConsultantPerformanceWithTargetByManager(@Param("periodId") Long periodId, @Param("managerId") Long managerId);

    @Query("""
        SELECT r.assignedConsultant.id, r.assignedConsultant.fullName,
               MIN(r.billPrintedAt),
               COUNT(r)
        FROM CustomerBillingRecord r
        WHERE r.collectedAt >= :startOfDay AND r.collectedAt < :endOfDay
          AND r.collectionStatus = 'DA_THANH_TOAN'
          AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
        GROUP BY r.assignedConsultant.id, r.assignedConsultant.fullName
        """)
    List<Object[]> getConsultantDailyStatsByManager(@Param("startOfDay") java.time.Instant startOfDay, @Param("endOfDay") java.time.Instant endOfDay, @Param("managerId") Long managerId);
}


