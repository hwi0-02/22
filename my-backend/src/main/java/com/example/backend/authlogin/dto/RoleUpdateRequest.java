package com.example.backend.authlogin.dto;

import com.example.backend.authlogin.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleUpdateRequest {
    private User.Role role;
}
