package vn.viettel.khdn.billing_platform.model.dto;

import java.time.Instant;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;

public record ResUserDTO(
    Long id,
    String username,
    String fullName,
    String phone,
    String status,
    RoleEnum role,
    Long regionId,
    String regionName,
    Long managerId,
    String managerName,
    Instant createdAt,
    Instant updatedAt
) {}
