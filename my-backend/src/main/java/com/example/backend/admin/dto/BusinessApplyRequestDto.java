package com.example.backend.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BusinessApplyRequestDto {
    private String businessName;
    private String businessNumber;
    private String address;
    private String phone;
}