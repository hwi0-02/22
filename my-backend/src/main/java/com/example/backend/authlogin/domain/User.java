package com.example.backend.authlogin.domain;

import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String password;
    private String address;
    
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    public enum Role {
        USER, ADMIN, BUSINESS
    }

    public enum Provider {
        LOCAL, GOOGLE, NAVER, KAKAO
    }

    @Builder
    public User(String name, String email, String password, String phone, String address, LocalDate dateOfBirth, Provider provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.role = Role.USER;
        this.provider = provider != null ? provider : Provider.LOCAL;
        this.createdOn = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (role == null) role = Role.USER;
        if (provider == null) provider = Provider.LOCAL;
    }
    
    public User update(String name, String phone, String address) {
        this.name = name;
        if (phone != null) {
            this.phone = phone;
        }
        if (address != null) {
            this.address = address;
        }
        return this;
    }
    
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setRole(Role role) { this.role = role; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}
