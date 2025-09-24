package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Business;
import com.example.backend.admin.dto.BusinessAdminDto;
import com.example.backend.admin.repository.BusinessRepository;
import com.example.backend.admin.service.AdminBusinessService;
import com.example.backend.admin.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.backend.admin.security.AdminGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/businesses")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminBusinessController {

    private final BusinessRepository businessRepository;
    private final AdminBusinessService adminBusinessService;
    private final BusinessService businessService;
    private final AdminGuard adminGuard;

    @GetMapping
    public ResponseEntity<Page<BusinessAdminDto>> list(
            Authentication authentication,
            @RequestParam(required = false) Business.BusinessStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        Page<BusinessAdminDto> businesses = adminBusinessService.getBusinesses(status, pageable);
        return ResponseEntity.ok(businesses);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        try {
            Business business = businessService.updateBusinessStatus(id, Business.BusinessStatus.APPROVED);
            return ResponseEntity.ok(Map.of("message", "approved", "id", id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminGuard.requireAdmin(authentication);
        String reason = body != null ? String.valueOf(body.get("reason")) : null;
        try {
            Business business = businessService.updateBusinessStatus(id, Business.BusinessStatus.REJECTED);
            return ResponseEntity.ok(Map.of("message", "rejected", "id", id, "reason", reason));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
