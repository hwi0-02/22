package com.example.backend.admin.domain;

import com.example.backend.authlogin.domain.User;
import com.example.backend.fe_hotel_detail.domain.Hotel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "business")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_number", nullable = false, unique = true, length = 50)
    private String businessNumber;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Hotel 엔터티에 business 필드가 아직 없어 매핑을 보류합니다.
    // 필요 시 Hotel 쪽 매핑 정리 후 양방향 연관관계를 복원하세요.

    public enum BusinessStatus {
        PENDING, APPROVED, REJECTED, SUSPENDED
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = BusinessStatus.PENDING;
        }
    }
}
