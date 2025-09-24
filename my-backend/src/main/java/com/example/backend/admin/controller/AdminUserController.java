package com.example.backend.admin.controller;

import com.example.backend.admin.dto.UserAdminDto;
import com.example.backend.admin.security.AdminGuard;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.dto.RoleUpdateRequest;
import com.example.backend.authlogin.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final LoginRepository userRepository;
    private final AdminGuard adminGuard;

    @GetMapping
    public ResponseEntity<Page<UserAdminDto>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) User.Role role,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        adminGuard.requireAdmin(authentication);
        Page<User> users = userRepository.findUsersWithFilters(name, email, role, pageable);
        return ResponseEntity.ok(users.map(UserAdminDto::from));
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserAdminDto> getUserDetail(
            @PathVariable Long userId,
            Authentication authentication) {
        adminGuard.requireAdmin(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(UserAdminDto.from(user));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request,
            Authentication authentication) {
        adminGuard.requireAdmin(authentication);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String currentEmail = authentication != null ? authentication.getName() : null;
        if (currentEmail != null && target.getEmail().equals(currentEmail)) {
            return ResponseEntity.badRequest().body(Map.of("error", "자신의 권한은 변경할 수 없습니다."));
        }
        User.Role oldRole = target.getRole();
        target.setRole(request.getRole());
        userRepository.save(target);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "권한이 변경되었습니다.");
        resp.put("userId", userId);
        resp.put("oldRole", oldRole);
        resp.put("newRole", request.getRole());
        return ResponseEntity.ok(resp);
    }
}
