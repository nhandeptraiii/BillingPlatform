package vn.viettel.khdn.billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.viettel.khdn.billing_platform.model.BillingPeriod;
import vn.viettel.khdn.billing_platform.model.Region;
import vn.viettel.khdn.billing_platform.model.RegionTarget;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.dto.ReqRegionTargetDTO;
import vn.viettel.khdn.billing_platform.model.dto.ResRegionTargetDTO;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;
import vn.viettel.khdn.billing_platform.repository.BillingPeriodRepository;
import vn.viettel.khdn.billing_platform.repository.RegionRepository;
import vn.viettel.khdn.billing_platform.repository.RegionTargetRepository;
import vn.viettel.khdn.billing_platform.util.SecurityUtil;
import vn.viettel.khdn.billing_platform.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionTargetService {

    private final RegionTargetRepository regionTargetRepository;
    private final RegionRepository regionRepository;
    private final BillingPeriodRepository billingPeriodRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new EntityNotFoundException("Chưa đăng nhập"));
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
    }

    public List<ResRegionTargetDTO> getRegionTargets(Long periodId, Long regionId) {
        // Có thể mở rộng phương thức query theo các filter, hiện tại tạm lấy tất cả và lọc
        List<RegionTarget> targets = regionTargetRepository.findAll();
        
        return targets.stream()
                .filter(t -> periodId == null || t.getBillingPeriod().getId().equals(periodId))
                .filter(t -> regionId == null || t.getRegion().getId().equals(regionId))
                .map(ResRegionTargetDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResRegionTargetDTO upsertRegionTarget(ReqRegionTargetDTO req) {
        User currentUser = getCurrentUser();
        
        // Kiểm tra quyền: Chỉ GĐKV (MANAGER) mới được nhập cho khu vực của họ, ADMIN được nhập cho tất cả
        if (currentUser.getRole() != RoleEnum.MANAGER && currentUser.getRole() != RoleEnum.ADMIN) {
            throw new IllegalArgumentException("Không có quyền thiết lập chỉ tiêu khu vực");
        }
        
        Long targetRegionId = null;
        if (currentUser.getRole() == RoleEnum.MANAGER) {
            if (currentUser.getRegion() == null) {
                throw new IllegalArgumentException("Giám đốc chưa được gán khu vực");
            }
            targetRegionId = currentUser.getRegion().getId();
        } else if (currentUser.getRole() == RoleEnum.ADMIN) {
            if (req.getRegionId() != null) {
                targetRegionId = req.getRegionId();
            } else if (currentUser.getRegion() != null) {
                targetRegionId = currentUser.getRegion().getId();
            } else {
                throw new IllegalArgumentException("Vui lòng truyền regionId cho khu vực cần thiết lập chỉ tiêu");
            }
        } else {
            throw new IllegalArgumentException("Không có quyền thiết lập chỉ tiêu khu vực");
        }

        BillingPeriod period = billingPeriodRepository.findById(req.getBillingPeriodId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kỳ cước"));
        
        Region region = regionRepository.findById(targetRegionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực"));

        Optional<RegionTarget> optTarget = regionTargetRepository.findByRegionIdAndBillingPeriodId(targetRegionId, period.getId());
        RegionTarget target;
        
        if (optTarget.isPresent()) {
            target = optTarget.get();
        } else {
            target = new RegionTarget();
            target.setRegion(region);
            target.setBillingPeriod(period);
        }
        
        target.setTargetCustomerPercent(req.getTargetCustomerPercent());
        target.setTargetRevenuePercent(req.getTargetRevenuePercent());
        target.setUpdatedBy(currentUser);
        
        target = regionTargetRepository.save(target);
        return new ResRegionTargetDTO(target);
    }
}
