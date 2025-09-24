package com.example.backend.authlogin.config;

import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        String adminEmail = "admin@hotel.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123!"))
                    .name("Admin")
                    .phone("010-0000-0000")
                    .address("System Admin Address")
                    .dateOfBirth(java.time.LocalDate.of(1985, 1, 1))
                    .provider(User.Provider.LOCAL)
                    .build();
            
            // Set role to ADMIN after building (builder sets USER by default)
            adminUser.setRole(User.Role.ADMIN);
            
            userRepository.save(adminUser);
            log.info("Admin account created successfully for email: {}", adminEmail);
        } else {
            log.info("Admin account already exists; skipping creation.");
        }
    }
}