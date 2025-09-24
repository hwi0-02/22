package com.example.backend.admin.service;

import com.example.backend.admin.dto.BusinessApplyRequestDto;
import com.example.backend.admin.domain.Business;
import com.example.backend.admin.repository.BusinessRepository;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    @Transactional
    public void applyBusiness(String userEmail, BusinessApplyRequestDto request) {
        log.info("사업자 신청 - 이메일: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 역할이 USER가 아니면 중복 신청 방지
        if (user.getRole() != User.Role.USER) {
            throw new IllegalStateException("이미 사업자 신청을 하거나 다른 권한을 가지고 있습니다.");
        }

        // 입력값 검증
        if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
            throw new IllegalArgumentException("상호명을 입력해주세요.");
        }
        if (request.getBusinessNumber() == null || request.getBusinessNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("사업자번호를 입력해주세요.");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("주소를 입력해주세요.");
        }
        // 간단한 사업자번호 형식 체크(숫자/하이픈 10~12자)
        String bn = request.getBusinessNumber().trim();
        if (!bn.matches("[0-9-]{10,12}")) {
            throw new IllegalArgumentException("유효한 사업자번호 형식이 아닙니다.");
        }

        // 이미 신청한 사업자 정보가 있는지 확인
        if (businessRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("이미 사업자 신청을 완료했습니다.");
        }

        // 동일 사업자번호 중복 방지
        if (businessRepository.existsByBusinessNumber(bn)) {
            throw new IllegalArgumentException("이미 등록된 사업자번호입니다.");
        }

        // Business 엔티티 생성 및 PENDING 상태로 설정
        Business business = Business.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessNumber(request.getBusinessNumber())
                .address(request.getAddress())
                .phone(request.getPhone())
                .status(Business.BusinessStatus.PENDING)
                .build();

        Business savedBusiness = businessRepository.save(business);
        log.info("사업자 정보 저장 - ID: {}", savedBusiness.getId());

        // 사용자 역할을 BUSINESS로 변경(사업자 신청 후)
        user.setRole(User.Role.BUSINESS);
        User updatedUser = userRepository.save(user);
        
        log.info("사용자 역할 변경 - {}", updatedUser.getRole());
    }

    /**
     * 사업자 상태 변경 로직은 트랜잭션 내에서 처리
     */
    @Transactional
    public Business updateBusinessStatus(Long businessId, Business.BusinessStatus newStatus) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("사업자를 찾을 수 없습니다."));

        Business.BusinessStatus oldStatus = business.getStatus();
        business.setStatus(newStatus);

        if (newStatus == Business.BusinessStatus.APPROVED) {
            User bu = business.getUser();
            if (bu != null && bu.getRole() != User.Role.ADMIN && bu.getRole() != User.Role.BUSINESS) {
                User.Role prev = bu.getRole();
                bu.setRole(User.Role.BUSINESS);
                userRepository.save(bu);
                log.info("사용자 역할 승격 - userId: {}, {} -> {} (businessId: {})", bu.getId(), prev, bu.getRole(), businessId);
            }
        }

        log.info("사업자 상태 변경 - businessId: {}, {} -> {}", businessId, oldStatus, newStatus);
        return business;
    }
}