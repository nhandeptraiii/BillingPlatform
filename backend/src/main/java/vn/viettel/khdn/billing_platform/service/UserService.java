package vn.viettel.khdn.billing_platform.service;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserCreateDTO;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserUpdateDTO;
import vn.viettel.khdn.billing_platform.model.dto.ReqUserUpdateMeDTO;
import vn.viettel.khdn.billing_platform.model.dto.ResUserDTO;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;
import vn.viettel.khdn.billing_platform.repository.BillingPeriodRepository;
import vn.viettel.khdn.billing_platform.repository.CustomerBillingRecordRepository;
import vn.viettel.khdn.billing_platform.repository.RegionRepository;
import vn.viettel.khdn.billing_platform.repository.RegionTargetRepository;
import vn.viettel.khdn.billing_platform.repository.UserRepository;
import vn.viettel.khdn.billing_platform.util.error.TransferRequiredException;
import vn.viettel.khdn.billing_platform.model.Region;

@Service
@Transactional
@SuppressWarnings("null")
public class UserService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerBillingRecordRepository recordRepository;
    private final BillingPeriodRepository billingPeriodRepository;
    private final RegionTargetRepository regionTargetRepository;

    public UserService(UserRepository userRepository, RegionRepository regionRepository,
                       PasswordEncoder passwordEncoder, CustomerBillingRecordRepository recordRepository,
                       BillingPeriodRepository billingPeriodRepository, RegionTargetRepository regionTargetRepository) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.passwordEncoder = passwordEncoder;
        this.recordRepository = recordRepository;
        this.billingPeriodRepository = billingPeriodRepository;
        this.regionTargetRepository = regionTargetRepository;
    }

    @Transactional(readOnly = true)
    public List<ResUserDTO> getAll() {
        return userRepository.findAll().stream()
            .map(this::convertToResUserDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<ResUserDTO> searchUsers(ResUserDTO currentUser, RoleEnum role, String keyword, Long managerId, boolean unassigned, Pageable pageable) {
        Long regionId = currentUser.role() == RoleEnum.ADMIN ? null : currentUser.regionId();
        Long effectiveManagerId;
        boolean effectiveUnassigned = unassigned;

        if (currentUser.role() == RoleEnum.NVKD) {
            if (unassigned) {
                // NVKD xem danh sách tư vấn viên chưa có quản lý trong địa bàn để tự nhận
                effectiveManagerId = null;
                effectiveUnassigned = true;
            } else {
                // Mặc định NVKD chỉ xem danh sách tư vấn viên thuộc nhóm mình
                effectiveManagerId = currentUser.id();
                effectiveUnassigned = false;
            }
        } else {
            effectiveManagerId = managerId;
        }

        return userRepository.searchUsers(regionId, role, effectiveManagerId, effectiveUnassigned, keyword, pageable)
            .map(this::convertToResUserDTO);
    }

    @Transactional(readOnly = true)
    public ResUserDTO getById(Long id, ResUserDTO currentUser) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));

        if (currentUser.role() == RoleEnum.NVKD) {
            boolean isSelf = user.getId().equals(currentUser.id());
            boolean isConsultantSameRegion = user.getRole() == RoleEnum.CONSULTANT
                && user.getRegion() != null && user.getRegion().getId().equals(currentUser.regionId());
            if (!isSelf && !isConsultantSameRegion) {
                throw new IllegalArgumentException("Bạn không có quyền xem thông tin người dùng này");
            }
        } else if (currentUser.role() == RoleEnum.MANAGER) {
            boolean isSelf = user.getId().equals(currentUser.id());
            boolean sameRegion = user.getRegion() != null && user.getRegion().getId().equals(currentUser.regionId());
            if (!isSelf && !sameRegion) {
                throw new IllegalArgumentException("Người dùng này không thuộc khu vực của bạn");
            }
        }

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
        } else if (currentUser.role() == RoleEnum.MANAGER) {
            if (req.role() != RoleEnum.NVKD && req.role() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("GĐKV chỉ được tạo tài khoản NVKD hoặc CONSULTANT");
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
                if (manager.getRole() != RoleEnum.NVKD || manager.getRegion() == null || !manager.getRegion().getId().equals(currentUser.regionId())) {
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
            if (req.role() != null && req.role() != RoleEnum.NVKD && req.role() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("GĐKV chỉ được phân quyền NVKD hoặc CONSULTANT");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: chỉ được sửa CONSULTANT trong nhóm của mình
            if (user.getRole() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được sửa tài khoản CONSULTANT");
            }
            if (user.getManager() == null || !user.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên này không thuộc nhóm của bạn");
            }
            if (req.role() != null && req.role() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD không được thay đổi quyền của tư vấn viên");
            }
            if (req.username() != null && !req.username().isBlank() && !req.username().trim().equals(user.getUsername())) {
                throw new IllegalArgumentException("NVKD không được thay đổi tên đăng nhập của tư vấn viên");
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
        if (req.role() != null && currentUser.role() != RoleEnum.NVKD) {
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

    public ResUserDTO setStatus(Long id, String status, ResUserDTO currentUser) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + id));
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }

        if (currentUser.role() == RoleEnum.MANAGER) {
            if (user.getRole() == RoleEnum.MANAGER || user.getRole() == RoleEnum.ADMIN) {
                throw new IllegalArgumentException("GĐKV không được đổi trạng thái tài khoản cấp trên");
            }
            if (user.getRegion() == null || !user.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Nhân viên này không thuộc khu vực của bạn");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
            if (user.getRole() != RoleEnum.CONSULTANT) {
                throw new IllegalArgumentException("NVKD chỉ được đổi trạng thái tài khoản CONSULTANT");
            }
            if (user.getManager() == null || !user.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên này không thuộc nhóm của bạn");
            }
        } else if (currentUser.role() == RoleEnum.ADMIN) {
            if (user.getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình");
            }
        }

        user.setStatus(status);
        User saved = userRepository.save(user);
        return convertToResUserDTO(saved);
    }

    public ResUserDTO assignManager(Long userId, Long managerId, ResUserDTO currentUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng ID: " + userId));

        if (user.getRole() != RoleEnum.CONSULTANT) {
            throw new IllegalArgumentException("Chỉ có thể gán người quản lý cho tài khoản CONSULTANT");
        }

        if (currentUser.role() == RoleEnum.NVKD) {
            // NVKD: được tự nhận tư vấn viên chưa có quản lý trong cùng khu vực
            if (user.getRegion() == null || !user.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Tư vấn viên không thuộc khu vực của bạn");
            }
            if (managerId == null || !managerId.equals(currentUser.id())) {
                throw new IllegalArgumentException("NVKD chỉ có quyền tự nhận tư vấn viên vào nhóm của mình");
            }
            if (user.getManager() != null && !user.getManager().getId().equals(currentUser.id())) {
                throw new IllegalArgumentException("Tư vấn viên đang thuộc quản lý của NVKD khác. Chỉ Quản lý khu vực (MANAGER) mới có quyền chuyển nhóm.");
            }
            User manager = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản NVKD"));
            user.setManager(manager);
        } else if (currentUser.role() == RoleEnum.MANAGER) {
            if (user.getRegion() == null || !user.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Tư vấn viên không thuộc khu vực của bạn");
            }
            if (managerId == null) {
                user.setManager(null);
            } else {
                User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người quản lý ID: " + managerId));
                if (manager.getRole() != RoleEnum.NVKD || manager.getRegion() == null || !manager.getRegion().getId().equals(currentUser.regionId())) {
                    throw new IllegalArgumentException("Người quản lý được gán phải là NVKD thuộc cùng khu vực");
                }
                user.setManager(manager);
            }
        } else if (currentUser.role() == RoleEnum.ADMIN) {
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
        } else {
            throw new IllegalArgumentException("Bạn không có quyền gán quản lý");
        }

        return convertToResUserDTO(userRepository.save(user));
    }

    public void delete(Long id, Long reassignToId, ResUserDTO currentUser) {
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

        // ADMIN không được xóa ADMIN khác
        if (currentUser.role() == RoleEnum.ADMIN && target.getRole() == RoleEnum.ADMIN) {
            throw new IllegalArgumentException("Không thể xóa tài khoản Admin");
        }

        // Kiểm tra xem target còn hồ sơ thu cước được giao không
        long assignedCount = recordRepository.countByAssignedConsultantId(id);
        if (assignedCount > 0) {
            if (reassignToId != null) {
                User reassignTo = userRepository.findById(reassignToId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tư vấn viên nhận lại hồ sơ ID: " + reassignToId));
                if (reassignTo.getId().equals(id)) {
                    throw new IllegalArgumentException("Không thể chuyển hồ sơ cho chính người đang bị xóa");
                }
                if (reassignTo.getRole() != RoleEnum.CONSULTANT || !"ACTIVE".equals(reassignTo.getStatus())) {
                    throw new IllegalArgumentException("Tư vấn viên nhận lại hồ sơ phải có vai trò CONSULTANT và đang hoạt động");
                }
                if (target.getRegion() != null && (reassignTo.getRegion() == null || !reassignTo.getRegion().getId().equals(target.getRegion().getId()))) {
                    throw new IllegalArgumentException("Tư vấn viên nhận lại hồ sơ phải thuộc cùng khu vực");
                }
                recordRepository.reassignConsultant(id, reassignTo);
            } else {
                throw new TransferRequiredException(
                    "Tư vấn viên đang phụ trách " + assignedCount + " hồ sơ thu cước. Vui lòng chuyển giao hồ sơ trước khi xóa.",
                    assignedCount
                );
            }
        }

        // Kiểm tra lịch sử giao dịch/hoạt động
        boolean hasHistory = recordRepository.existsByCollectedById(id)
                || recordRepository.existsByDebtMarkedById(id)
                || billingPeriodRepository.existsByCreatedById(id)
                || regionTargetRepository.existsByUpdatedById(id);

        if (hasHistory) {
            // Đã có lịch sử thu/gạch nợ -> chuyển sang vô hiệu hóa INACTIVE (không xóa cứng)
            target.setStatus("INACTIVE");
            userRepository.save(target);
            return;
        }

        // Nếu xóa NVKD: SET NULL manager_id cho các CONSULTANT trong nhóm
        if (target.getRole() == RoleEnum.NVKD) {
            userRepository.clearManagerId(id);
        }

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
            if (target.getRole() == RoleEnum.MANAGER || target.getRole() == RoleEnum.ADMIN) {
                throw new IllegalArgumentException("GĐKV không được đặt lại mật khẩu tài khoản cấp trên");
            }
            if (target.getRegion() == null || !target.getRegion().getId().equals(currentUser.regionId())) {
                throw new IllegalArgumentException("Nhân viên này không thuộc khu vực của bạn");
            }
        } else if (currentUser.role() == RoleEnum.NVKD) {
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

                if (phone != null && phone.length() == 9 && !phone.startsWith("0")) {
                    phone = "0" + phone;
                }

                if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
                    continue; // Bỏ qua nếu SĐT không hợp lệ (bắt đầu bằng 0, độ dài 10 số)
                }

                if (userRepository.existsByUsername(username)) {
                    continue; // Bỏ qua user đã tồn tại
                }

                try {
                    User user = new User();
                    user.setUsername(username);
                    user.setFullName(fullName);
                    user.setPhone(phone);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setRole(RoleEnum.CONSULTANT);
                    user.setStatus("ACTIVE");

                    if (region != null) {
                        user.setRegion(region);
                    } else {
                        user.setRegion(null);
                    }

                    // NVKD import -> tự động set manager là NVKD đó
                    if (currentUser.role() == RoleEnum.NVKD) {
                        userRepository.findById(currentUser.id()).ifPresent(user::setManager);
                    }

                    userRepository.save(user);
                    count++;
                } catch (Exception ignored) {
                }
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
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    String strVal = String.valueOf((long) val);
                    if (strVal.length() == 9 && !strVal.startsWith("0")) {
                        yield "0" + strVal;
                    }
                    yield strVal;
                }
                yield String.valueOf(val);
            }
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
