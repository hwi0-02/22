package com.example.backend.authlogin.config;

import com.example.backend.admin.domain.Business;
import com.example.backend.authlogin.domain.User;
import com.example.backend.admin.repository.BusinessRepository;
import com.example.backend.authlogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessRoleReconciler implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @Override
    public void run(String... args) {
        // 1) 승인된 사업자의 사용자들은 BUSINESS 역할을 유지
        // 2) 그러나 BUSINESS 역할인데 승인된 Business가 없는 사용자는 USER로 다운그레이드
        List<User> users = userRepository.findAll();
        int fixed = 0;
        for (User u : users) {
            if (u.getRole() == User.Role.BUSINESS) {
                Business b = businessRepository.findByUser(u).orElse(null);
                boolean approved = b != null && b.getStatus() == Business.BusinessStatus.APPROVED;
                if (!approved) {
                    u.setRole(User.Role.USER);
                    fixed++;
                }
            }
        }
        if (fixed > 0) {
            userRepository.saveAll(users);
            log.info("사업자 권한 동기화 완료: {}명의 사용자가 USER 권한으로 다운그레이드되었습니다.", fixed);
        } else {
            log.info("사업자 권한 동기화: 변경할 사용자가 없습니다.");
        }
    }
}