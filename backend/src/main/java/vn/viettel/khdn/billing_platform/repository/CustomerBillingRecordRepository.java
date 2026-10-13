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

    // =====================================================================
    // THỐNG KÊ DASHBOARD
    // Quy ước "đã thu" (thống nhất mọi nơi):
    //  - Số KH đã thu  = số hồ sơ debtStatus = DA_GACH_NO
    //  - Số tiền đã thu = tổng tiền của hồ sơ DA_GACH_NO, cắt trần ở amountDue
    //    (collectedAmount null/0 mà đã gạch nợ => tính bằng amountDue)
    // =====================================================================
    String GACH_NO_AMOUNT = """
            SUM(CASE WHEN r.debtStatus = 'DA_GACH_NO' THEN (
                    CASE WHEN r.collectedAmount IS NULL OR r.collectedAmount <= 0 THEN COALESCE(r.amountDue, 0)
                         WHEN r.collectedAmount > COALESCE(r.amountDue, 0) THEN COALESCE(r.amountDue, 0)
                         ELSE r.collectedAmount END)
                ELSE 0 END)
            """;
    String GACH_NO_COUNT = "SUM(CASE WHEN r.debtStatus = 'DA_GACH_NO' THEN 1 ELSE 0 END)";

    // Tiến độ theo kỳ (MANAGER/ADMIN)
    @Query("SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue), " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
            GROUP BY r.collectionStatus, r.debtStatus
            """)
    List<Object[]> getProgressByPeriod(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    // Tiến độ theo kỳ và tư vấn viên
    @Query("SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue), " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId AND r.assignedConsultant.id = :consultantId
            GROUP BY r.collectionStatus, r.debtStatus
            """)
    List<Object[]> getProgressByPeriodAndConsultant(@Param("periodId") Long periodId,
            @Param("consultantId") Long consultantId);

    // Tiến độ theo kỳ cho NVKD (nhóm tư vấn viên + hồ sơ giao thẳng cho NVKD)
    @Query("SELECT r.collectionStatus, r.debtStatus, COUNT(r), SUM(r.amountDue), " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
            GROUP BY r.collectionStatus, r.debtStatus
            """)
    List<Object[]> getProgressByPeriodAndManager(@Param("periodId") Long periodId,
            @Param("managerId") Long managerId);

    // FTTH N1: [tổng hồ sơ, tổng phải thu, số hồ sơ đã gạch nợ, số tiền đã gạch nợ]
    @Query("SELECT COUNT(r), SUM(r.amountDue), " + GACH_NO_COUNT + ", " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
              AND r.ftthN1 = true
            """)
    List<Object[]> getFtthN1Stats(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    @Query("SELECT COUNT(r), SUM(r.amountDue), " + GACH_NO_COUNT + ", " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND (r.assignedConsultant.manager.id = :managerId OR r.assignedConsultant.id = :managerId)
              AND r.ftthN1 = true
            """)
    List<Object[]> getFtthN1StatsByManager(@Param("periodId") Long periodId, @Param("managerId") Long managerId);

    @Query("SELECT COUNT(r), SUM(r.amountDue), " + GACH_NO_COUNT + ", " + GACH_NO_AMOUNT + """
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND r.assignedConsultant.id = :consultantId
              AND r.ftthN1 = true
            """)
    List<Object[]> getFtthN1StatsByConsultant(@Param("periodId") Long periodId,
            @Param("consultantId") Long consultantId);

    /**
     * Thống kê theo từng tư vấn viên (LEFT JOIN từ User nên tư vấn viên chưa có hồ sơ vẫn hiện).
     * Cột: [0] id, [1] họ tên, [2] role, [3] managerId, [4] managerName,
     *      [5] tổng hồ sơ, [6] tổng phải thu, [7] hồ sơ đã gạch nợ, [8] tiền đã gạch nợ,
     *      [9] FTTH N1 tổng hồ sơ, [10] FTTH N1 phải thu, [11] FTTH N1 đã gạch nợ, [12] FTTH N1 tiền đã gạch nợ
     * regionId != null: lọc theo khu vực (MANAGER); managerId != null: chỉ nhóm của NVKD đó.
     */
    @Query("""
            SELECT c.id, c.fullName, c.role, m.id, m.fullName,
                   COUNT(r), COALESCE(SUM(r.amountDue), 0),
                   COALESCE(""" + GACH_NO_COUNT + """
                   , 0),
                   COALESCE(""" + GACH_NO_AMOUNT + """
                   , 0),
                   COALESCE(SUM(CASE WHEN r.ftthN1 = true THEN 1 ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN r.ftthN1 = true THEN r.amountDue ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN r.ftthN1 = true AND r.debtStatus = 'DA_GACH_NO' THEN 1 ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN r.ftthN1 = true AND r.debtStatus = 'DA_GACH_NO' THEN (
                        CASE WHEN r.collectedAmount IS NULL OR r.collectedAmount <= 0 THEN COALESCE(r.amountDue, 0)
                             WHEN r.collectedAmount > COALESCE(r.amountDue, 0) THEN COALESCE(r.amountDue, 0)
                             ELSE r.collectedAmount END) ELSE 0 END), 0)
            FROM User c
            LEFT JOIN c.manager m
            LEFT JOIN CustomerBillingRecord r ON r.assignedConsultant.id = c.id AND r.billingPeriod.id = :periodId
            WHERE (c.role = 'CONSULTANT' OR (c.role = 'NVKD' AND r.id IS NOT NULL))
              AND (c.status = 'ACTIVE' OR r.id IS NOT NULL)
              AND (cast(:regionId as Long) IS NULL OR c.region.id = :regionId)
              AND (cast(:managerId as Long) IS NULL OR m.id = :managerId OR c.id = :managerId)
            GROUP BY c.id, c.fullName, c.role, m.id, m.fullName
            """)
    List<Object[]> getConsultantStats(@Param("periodId") Long periodId,
            @Param("regionId") Long regionId, @Param("managerId") Long managerId);

    // Hồ sơ chưa gán tư vấn viên (để tổng các dòng khớp tổng overview của MANAGER/ADMIN)
    // Cột: [0] tổng hồ sơ, [1] tổng phải thu, [2] đã gạch nợ, [3] tiền đã gạch nợ
    @Query("SELECT COUNT(r), COALESCE(SUM(r.amountDue), 0), COALESCE(" + GACH_NO_COUNT + ", 0), COALESCE("
            + GACH_NO_AMOUNT + """
            , 0)
            FROM CustomerBillingRecord r
            WHERE r.billingPeriod.id = :periodId
              AND r.assignedConsultant IS NULL
              AND (cast(:regionId as Long) IS NULL OR r.region.id = :regionId)
            """)
    List<Object[]> getUnassignedStats(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    // Thống kê giờ in bill đầu tiên và số lượng thu trong ngày của các tư vấn viên
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

    // =====================================================================
    // XÓA KỲ THEO KHU VỰC / LỊCH SỬ NGƯỜI DÙNG
    // =====================================================================
    boolean existsByBillingPeriodId(Long billingPeriodId);

    long countByBillingPeriodId(Long billingPeriodId);


    long countByBillingPeriodIdAndRegionId(Long billingPeriodId, Long regionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CustomerBillingRecord r WHERE r.billingPeriod.id = :periodId AND r.region.id = :regionId")
    int deleteByBillingPeriodIdAndRegionId(@Param("periodId") Long periodId, @Param("regionId") Long regionId);

    boolean existsByAssignedConsultantId(Long userId);

    long countByAssignedConsultantId(Long userId);


    boolean existsByCollectedById(Long userId);

    boolean existsByDebtMarkedById(Long userId);

    // Chuyển toàn bộ hồ sơ của tư vấn viên cũ sang người nhận
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CustomerBillingRecord r SET r.assignedConsultant = :to WHERE r.assignedConsultant.id = :fromId")
    int reassignConsultant(@Param("fromId") Long fromId,
            @Param("to") vn.viettel.khdn.billing_platform.model.User to);
}
