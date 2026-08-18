package vn.viettel.khdn.billing_platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;

public record ReqUserUpdateDTO(
        // Có thể sửa username (nullable — chỉ update nếu không null)
        @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Họ tên không được để trống") String fullName,

        @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số") String phone,

        RoleEnum role,

        Long regionId) {
}
