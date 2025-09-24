package com.example.backend.admin.dto;

import com.example.backend.authlogin.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserAdminDto {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private User.Provider provider;
    private LocalDateTime createdOn;

    public static UserAdminDto from(User user) {
        return UserAdminDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .createdOn(user.getCreatedOn())
                .build();
    }
}
