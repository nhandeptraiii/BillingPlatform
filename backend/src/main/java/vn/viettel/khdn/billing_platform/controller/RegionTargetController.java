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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'NVKD', 'CONSULTANT')")
    public ResponseEntity<List<ResRegionTargetDTO>> getRegionTargets(
            @RequestParam(value = "periodId", required = false) Long periodId,
            @RequestParam(value = "regionId", required = false) Long regionId) {
        return ResponseEntity.ok(regionTargetService.getRegionTargets(periodId, regionId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<ResRegionTargetDTO> upsertRegionTarget(@Valid @RequestBody ReqRegionTargetDTO req) {
        return ResponseEntity.ok(regionTargetService.upsertRegionTarget(req));
    }
}
