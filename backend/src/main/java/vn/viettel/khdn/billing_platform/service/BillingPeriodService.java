package vn.viettel.khdn.billing_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import vn.viettel.khdn.billing_platform.model.BillingPeriod;
import vn.viettel.khdn.billing_platform.model.User;
import vn.viettel.khdn.billing_platform.model.enums.BillingPeriodStatusEnum;
import vn.viettel.khdn.billing_platform.model.enums.RoleEnum;
import vn.viettel.khdn.billing_platform.repository.BillingPeriodRepository;
import vn.viettel.khdn.billing_platform.repository.CustomerBillingRecordRepository;
import vn.viettel.khdn.billing_platform.repository.RegionTargetRepository;

@Service
public class BillingPeriodService {

    public record DeletePeriodResult(long deletedRecords, boolean periodDeleted, String message) {
    }

    private final BillingPeriodRepository periodRepository;
    private final CustomerBillingRecordRepository recordRepository;
    private final RegionTargetRepository regionTargetRepository;

    public BillingPeriodService(BillingPeriodRepository periodRepository,
            CustomerBillingRecordRepository recordRepository,
            RegionTargetRepository regionTargetRepository) {
        this.periodRepository = periodRepository;
        this.recordRepository = recordRepository;
        this.regionTargetRepository = regionTargetRepository;
    }

    /**
     * Xóa dữ liệu kỳ cước đã import.
     * - ADMIN: xóa toàn bộ dữ liệu của kỳ (mọi khu vực) + chỉ tiêu + kỳ.
     * - MANAGER: chỉ xóa dữ liệu & chỉ tiêu của KHU VỰC mình. Kỳ (dùng chung) chỉ bị xóa
     *   khi không còn dữ liệu/chỉ tiêu của khu vực nào khác.
     */
    @Transactional
    public DeletePeriodResult deletePeriod(Long id, User currentUser) {
        BillingPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kỳ ID: " + id));

        if (period.getStatus() == BillingPeriodStatusEnum.CLOSED) {
            throw new IllegalStateException("Không thể xóa kỳ đã đóng");
        }

        if (currentUser.getRole() == RoleEnum.ADMIN) {
            long total = recordRepository.countByBillingPeriodId(id);
            recordRepository.deleteAllByBillingPeriodId(id);
            regionTargetRepository.deleteAllByBillingPeriodId(id);
            periodRepository.delete(period);
            return new DeletePeriodResult(total, true, "Đã xóa toàn bộ kỳ " + period.getName());
        }

        if (currentUser.getRole() != RoleEnum.MANAGER) {
            throw new IllegalArgumentException("Không có quyền xóa kỳ cước");
        }
        if (currentUser.getRegion() == null) {
            throw new IllegalArgumentException("Tài khoản chưa được gán khu vực");
        }
        Long regionId = currentUser.getRegion().getId();

        long deleted = recordRepository.deleteByBillingPeriodIdAndRegionId(id, regionId);
        regionTargetRepository.deleteByBillingPeriodIdAndRegionId(id, regionId);

        boolean othersRemain = recordRepository.existsByBillingPeriodId(id)
                || regionTargetRepository.existsByBillingPeriodId(id);
        if (othersRemain) {
            return new DeletePeriodResult(deleted, false,
                    "Đã xóa " + deleted + " bản ghi của khu vực bạn. Kỳ vẫn được giữ vì còn dữ liệu của khu vực khác");
        }
        periodRepository.delete(period);
        return new DeletePeriodResult(deleted, true, "Đã xóa " + deleted + " bản ghi và kỳ " + period.getName());
    }
}
