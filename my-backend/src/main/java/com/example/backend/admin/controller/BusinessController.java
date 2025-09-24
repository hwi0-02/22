package com.example.backend.admin.controller;

import com.example.backend.admin.dto.BusinessApplyRequestDto;
import com.example.backend.admin.service.BusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/businesses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody BusinessApplyRequestDto request, Authentication authentication) {
        log.info("사업자 신청 API 호출");
        
        if (authentication == null) {
            log.error("인증 정보가 없습니다");
            return ResponseEntity.badRequest().body(Map.of("error", "인증이 필요합니다"));
        }
        
        try {
            businessService.applyBusiness(authentication.getName(), request);
            log.info("사업자 신청 성공");
            return ResponseEntity.ok(Map.of(
                    "message", "신청이 완료되었습니다. 관리자 승인 후 활동하실 수 있습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("사업자 신청 입력 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("사업자 신청 중복/상태 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사업자 신청 처리 중 예외", e);
            return ResponseEntity.badRequest().body(Map.of("error", "신청 처리 중 오류가 발생했습니다."));
        }
    }
}