package com.example.backend.admin.controller;

import com.example.backend.admin.dto.DashboardSummaryDto;
import com.example.backend.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 대시보드 요약 데이터 조회
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(Authentication authentication) {
        log.info("[AdminController] 대시보드 요약 데이터 조회 요청 - 사용자: {}", authentication.getName());
        
        try {
            DashboardSummaryDto summary = adminDashboardService.getDashboardSummary();
            log.info("[AdminController] 대시보드 요약 데이터 조회 성공");
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("[AdminController] 대시보드 요약 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
