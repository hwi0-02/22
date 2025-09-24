package com.example.backend.authlogin.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String dateOfBirth; // String으로 받아서 LocalDate로 변환
    private String address;
}
