package com.example.backend.fe_hotel_detail.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "Hotel")
@Getter @Setter @NoArgsConstructor
public class Hotel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, length=255)
    private String address;

    private Integer starRating;

    @Lob
    private String description;

    @Column(length=50)
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HotelStatus status = HotelStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum HotelStatus { PENDING, APPROVED, REJECTED, SUSPENDED }

}
