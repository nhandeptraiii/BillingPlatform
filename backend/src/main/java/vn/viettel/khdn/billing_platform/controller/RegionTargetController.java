package vn.viettel.khdn.billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.viettel.khdn.billing_platform.model.dto.ReqRegionTargetDTO;
import vn.viettel.khdn.billing_platform.model.dto.ResRegionTargetDTO;
import vn.viettel.khdn.billing_platform.service.RegionTargetService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/region-targets")
@RequiredArgsConstructor
public class RegionTargetController {

    private final RegionTargetService regionTargetService;

    private final vn.viettel.khdn.billing_platform.repository.BillingPeriodRepository periodRepository;

    private Long resolvePeriodId(Integer month, Integer year) {
        if (month == null || year == null) {
            java.time.LocalDate now = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
            if (month == null) month = now.getMonthValue();
            if (year == null) year = now.getYear();
        }
        return periodRepository.findByMonthAndYear(month, year)
            .map(vn.viettel.khdn.billing_platform.model.BillingPeriod::getId)
            .orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'NVKD', 'CONSULTANT')")
    public ResponseEntity<List<ResRegionTargetDTO>> getRegionTargets(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "regionId", required = false) Long regionId) {
        Long periodId = resolvePeriodId(month, year);
        if (periodId == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(regionTargetService.getRegionTargets(periodId, regionId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<ResRegionTargetDTO> upsertRegionTarget(@Valid @RequestBody ReqRegionTargetDTO req) {
        return ResponseEntity.ok(regionTargetService.upsertRegionTarget(req));
    }
}
