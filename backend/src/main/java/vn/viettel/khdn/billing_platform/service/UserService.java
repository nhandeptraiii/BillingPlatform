package vn.viettel.khdn.billing_platform.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import java.io.InputStream;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserCreateDTO;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserUpdateDTO;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserUpdateMeDTO;
import vn.viettel.khdn.billing_platform.model.dto.ResUserDTO;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;
import vn.viettel.khdn.billing_platform.repository.CustomerBillingRecordRepository;
import vn.viettel.khdn.billing_platform.repository.UserRepository;
import vn.viettel.khdn.billing_platform.repository.RegionRepository;
import vn.viettel.khdn.billing_platform.model.Region;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerBillingRecordRepository recordRepository;

    public UserService(UserRepository userRepository, RegionRepository regionRepository,
                       PasswordEncoder passwordEncoder, CustomerBillingRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.passwordEncoder = passwordEncoder;
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public List<ResUserDTO> getAll() {
        return userRepository.findAll().stream()
            .map(this::convertToResUserDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ResUserDTO> searchUsers(ResUserDTO currentUser, RoleEnum role, String keyword, Long managerId, Pageable pageable) {
        // ADMIN: xem tất cả; MANAGER & NVKD: chỉ xem trong khu vực của mình
        Long regionId = currentUser.role() == RoleEnum.ADMIN ? null : currentUser.regionId();
        // NVKD: tự động filter theo managerId của chính mình
        Long effectiveManagerId = currentUser.role() == RoleEnum.NVKD ? currentUser.id() : managerId;
        return userRepository.searchUsers(regionId, role, effectiveManagerId, keyword, pageable)
            .map(this::convertToResUserDTO);
    }

    @Transactional(readOnly = true)
    public ResUserDTO getById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));
        return convertToResUserDTO(user);
    }

    @Transactional(readOnly = true)
    public ResUserDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng Username: " + username));
        return convertToResUserDTO(user);
    }

    public ResUserDTO create(ReqUserCreateDTO req, ResUserDTO currentUser) {
        if (userRepository.existsByUsername(req.username())) {
            throw new EntityExistsException("Username đã tồn tại: " + req.username());
        }

        // Kiểm tra quyền tạo role:
        // MANAGER: tạo được NVKD và CONSULTANT
        // NVKD: chỉ tạo được CONSULTANT
        if (currentUser.role() == RoleEnum.NVKD) {
            if (req.role() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được tạo tài khoản CONSULTANT");
            }
        }

        User user = new User();
        user.setUsername(req.username());
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());

        if (currentUser.role() == RoleEnum.MANAGER) {
            // GĐKV: tạo NVKD hoặc CONSULTANT trong khu vực của mình
            Region region = regionRepository.findById(currentUser.regionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực của GĐKV"));
            user.setRegion(region);
            // GĐKV có thể gán CONSULTANT cho một NVKD cụ thể trong khu vực
            if (req.managerId() != null) {
                User manager = userRepository.findById(req.managerId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy NVKD ID: " + req.managerId()));
                if (manager.getRole() != RoleEnum.NVKD || !manager.getRegion().getId().equals(currentUser.regionId())) {
                    throw new IllegalArgumentException("NVKD không thuộc khu vực của bạn");
                }
                user.setManager(manager);
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: chỉ tạo CONSULTANT, gán khu vực và manager = NVKD này
            Region region = regionRepository.findById(currentUser.regionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực của NVKD"));
            user.setRegion(region);
            User manager = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng NVKD"));
            user.setManager(manager);
        } else {
            // ADMIN: toàn quyền, có thể chỉ định khu vực và manager tùy ý
            if (req.regionId() != null) {
                Region region = regionRepository.findById(req.regionId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực ID: " + req.regionId()));
                user.setRegion(region);
            } else if (req.role() != RoleEnum.ADMIN) {
                throw new IllegalArgumentException("Khu vực không được để trống khi tạo " + req.role());
            }
            if (req.managerId() != null) {
                User manager = userRepository.findById(req.managerId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người quản lý ID: " + req.managerId()));
                user.setManager(manager);
            }
        }

        user.setStatus("ACTIVE");
        User saved = userRepository.save(user);
        return convertToResUserDTO(saved);
    }

    public ResUserDTO update(Long id, ReqUserUpdateDTO req, ResUserDTO currentUser) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));

        if (currentUser.role() == RoleEnum.MANAGER) {
            // GĐKV: được sửa NVKD và CONSULTANT trong khu vực của mình
            if (user.getRole() == RoleEnum.MANAGER || user.getRole() == RoleEnum.ADMIN) {
                throw new IllegalArgumentException("GĐKV không được sửa tài khoản cấp trên");
            }
            if (user.getRegion() == null || !user.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Nhân viên này không thuộc khu vực của bạn");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: chỉ được sửa CONSULTANT trong nhóm của mình
            if (user.getRole() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được sửa tài khoản CONSULTANT");
            }
            if (user.getManager() == null || !user.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên này không thuộc nhóm của bạn");
            }
        }

        // Cập nhật username nếu có (kiểm tra trùng)
        if (req.username() != null && !req.username().isBlank()) {
            String newUsername = req.username().trim();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new EntityExistsException("Username '" + newUsername + "' đã tồn tại");
                }
                user.setUsername(newUsername);
            }
        }

        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        if (req.role() != null) {
            user.setRole(req.role());
        }

        RoleEnum targetRole = user.getRole();

        if (currentUser.role() == RoleEnum.MANAGER || currentUser.role() == RoleEnum.NVKD) {
            // Giữ nguyên region của người dùng (MANAGER/NVKD không đổi region cho CONSULTANT)
        } else if (currentUser.role() == RoleEnum.ADMIN) {
            if (req.regionId() != null) {
                Region region = regionRepository.findById(req.regionId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực ID: " + req.regionId()));
                user.setRegion(region);
            } else if (targetRole != RoleEnum.ADMIN) {
                // Giữ nguyên region cũ nếu đang có, hoặc báo lỗi nếu chưa có
                if (user.getRegion() == null) {
                    throw new IllegalArgumentException("Khu vực không được để trống khi cập nhật " + targetRole);
                }
            } else {
                user.setRegion(null);
            }
        }

        User saved = userRepository.save(user);
        return convertToResUserDTO(saved);
    }

    public ResUserDTO updateMe(String username, ReqUserUpdateMeDTO req) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng Username: " + username));
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        User saved = userRepository.save(user);
        return convertToResUserDTO(saved);
    }

    public ResUserDTO setStatus(Long id, String status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }
        user.setStatus(status);
        User saved = userRepository.save(user);
        return convertToResUserDTO(saved);
    }

    public ResUserDTO assignManager(Long userId, Long managerId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + userId));

        if (managerId == null) {
            user.setManager(null);
        } else {
            User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người quản lý ID: " + managerId));
            if (manager.getRole() != RoleEnum.NVKD && manager.getRole() != RoleEnum.MANAGER) {
                throw new IllegalArgumentException("Người quản lý phải có role NVKD hoặc MANAGER");
            }
            user.setManager(manager);
        }

        return convertToResUserDTO(userRepository.save(user));
    }

    public void delete(Long id, ResUserDTO currentUser) {
        User target = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));

        if (currentUser.role() == RoleEnum.MANAGER) {
            // GĐKV: được xóa NVKD và CONSULTANT trong khu vực
            if (target.getRole() == RoleEnum.MANAGER || target.getRole() == RoleEnum.ADMIN) {
                throw new IllegalArgumentException("GĐKV không được xóa tài khoản cấp trên");
            }
            if (target.getRegion() == null || !target.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Nhân viên này không thuộc khu vực của bạn");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: chỉ xóa được CONSULTANT trong nhóm của mình
            if (target.getRole() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được xóa tài khoản CONSULTANT");
            }
            if (target.getManager() == null || !target.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên này không thuộc nhóm của bạn");
            }
        }

        // ADMIN không được xóa ADMIN khác (bảo vệ)
        if (currentUser.role() == RoleEnum.ADMIN && target.getRole() == RoleEnum.ADMIN) {
            throw new IllegalArgumentException("Không thể xóa tài khoản Admin");
        }

        // Nếu xóa NVKD: SET NULL manager_id cho các CONSULTANT trong nhóm (Option A)
        // CONSULTANT vẫn giữ nguyên, thuộc khu vực nhưng manager = null (trực thuộc GĐKV)
        if (target.getRole() == RoleEnum.NVKD) {
            userRepository.clearManagerId(id);
        }

        // SET NULL assigned_consultant trên tất cả bản ghi thu cước trước khi xóa user
        recordRepository.clearAssignedConsultant(id);

        userRepository.deleteById(id);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + userId));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void resetPassword(Long userId, String newPassword, ResUserDTO currentUser) {
        User target = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + userId));

        if (currentUser.role() == RoleEnum.MANAGER) {
            // GĐKV: được reset password cho NVKD và CONSULTANT trong khu vực
            if (target.getRole() == RoleEnum.MANAGER || target.getRole() == RoleEnum.ADMIN) {
                throw new IllegalArgumentException("GĐKV không được đặt lại mật khẩu tài khoản cấp trên");
            }
            if (target.getRegion() == null || !target.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Nhân viên này không thuộc khu vực của bạn");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: chỉ reset password cho CONSULTANT trong nhóm của mình
            if (target.getRole() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được đặt lại mật khẩu cho tài khoản CONSULTANT");
            }
            if (target.getManager() == null || !target.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên này không thuộc nhóm của bạn");
            }
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
    }

    public int importConsultants(MultipartFile file, ResUserDTO currentUser) {
        if (currentUser.role() != RoleEnum.MANAGER
                && currentUser.role() != RoleEnum.ADMIN
                && currentUser.role() != RoleEnum.NVKD) {
            throw new IllegalArgumentException("Chỉ Quản lý, NVKD hoặc Admin mới được import nhân viên");
        }

        Region region = null;
        if (currentUser.role() == RoleEnum.MANAGER || currentUser.role() == RoleEnum.NVKD) {
            region = regionRepository.findById(currentUser.regionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực của người dùng"));
        }

        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String username = getCellValue(row.getCell(0));
                String fullName = getCellValue(row.getCell(1));
                String phone = getCellValue(row.getCell(2));

                if (username.isEmpty() || fullName.isEmpty()) {
                    continue; // Bỏ qua các dòng thiếu dữ liệu bắt buộc
                }

                if (phone == null || !phone.matches("^0\\d{9,10}$")) {
                    continue; // Bỏ qua nếu SĐT không hợp lệ (bắt đầu bằng 0, độ dài 10-11 số)
                }

                if (userRepository.existsByUsername(username)) {
                    continue; // Bỏ qua user đã tồn tại
                }

                User user = new User();
                user.setUsername(username);
                user.setFullName(fullName);
                user.setPhone(phone);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(RoleEnum.CONSULTANT);
                user.setStatus("ACTIVE");

                if (region != null) {
                    user.setRegion(region); // Manager/NVKD import -> gán khu vực
                } else {
                    user.setRegion(null); // Admin import -> Tạm thời không có khu vực
                }

                // NVKD import -> tự động set manager là NVKD đó
                if (currentUser.role() == RoleEnum.NVKD) {
                    userRepository.findById(currentUser.id()).ifPresent(user::setManager);
                }

                userRepository.save(user);
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage(), e);
        }
        return count;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private ResUserDTO convertToResUserDTO(User user) {
        return new ResUserDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getPhone(),
            user.getStatus(),
            user.getRole(),
            user.getRegion() != null ? user.getRegion().getId() : null,
            user.getRegion() != null ? user.getRegion().getName() : null,
            user.getManager() != null ? user.getManager().getId() : null,
            user.getManager() != null ? user.getManager().getFullName() : null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
