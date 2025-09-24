package com.example.backend.admin.service;

import com.example.backend.admin.domain.Business;
import com.example.backend.admin.dto.BusinessAdminDto;
import com.example.backend.admin.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBusinessService {

    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public Page<BusinessAdminDto> getBusinesses(Business.BusinessStatus status, Pageable pageable) {
        log.info("[AdminService] 사업자 목록 조회 - status: {}, page: {}", status, pageable.getPageNumber());

        boolean includeNull = (status == Business.BusinessStatus.PENDING);
        Page<Business> businesses = businessRepository.findBusinessesWithFilters(status, includeNull, pageable);

        log.debug("[AdminService] 조회된 사업자 수: {}", businesses.getTotalElements());
        return businesses.map(BusinessAdminDto::from);
    }
}