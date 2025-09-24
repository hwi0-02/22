package com.example.backend.admin.dto;

import com.example.backend.admin.domain.Business;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateRequest {
    private Business.BusinessStatus status;
}