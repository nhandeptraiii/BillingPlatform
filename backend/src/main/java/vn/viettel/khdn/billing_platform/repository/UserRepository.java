package vn.viettel.khdn.billing_platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;

public interface UserRepository extends JpaRepository<User, Long> {

    // ---- Tìm theo username (định danh đăng nhập chính) ----
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRoleOrderByIdAsc(RoleEnum role);

    Optional<User> findFirstByRegionIdAndRoleOrderByIdAsc(Long regionId, RoleEnum role);

    // SET NULL manager_id cho tất cả CONSULTANT của NVKD bị xóa
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE User u SET u.manager = NULL WHERE u.manager.id = :managerId")
    void clearManagerId(@Param("managerId") Long managerId);

    @Query(value = """
            SELECT u FROM User u
            LEFT JOIN u.region reg
            LEFT JOIN u.manager m
            WHERE (:role IS NULL OR u.role = :role)
              AND (cast(:regionId as Long) IS NULL OR reg.id = :regionId
                   OR EXISTS (SELECT 1 FROM CustomerBillingRecord r WHERE r.assignedConsultant = u AND r.region.id = :regionId))
              AND (cast(:managerId as Long) IS NULL OR m.id = :managerId)
              AND (:unassigned = false OR m IS NULL OR m.role = 'MANAGER')
              AND (
                :keyword IS NULL
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR u.phone LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<User> searchUsers(
            @Param("regionId") Long regionId,
            @Param("role") RoleEnum role,
            @Param("managerId") Long managerId,
            @Param("unassigned") boolean unassigned,
            @Param("keyword") String keyword,
            Pageable pageable);

}
