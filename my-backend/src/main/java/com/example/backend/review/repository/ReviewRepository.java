package com.example.backend.review.repository;

import com.example.backend.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 기존 메소드들
    Page<Review> findByHiddenFalse(Pageable pageable);
    long countByHiddenTrue();
    long countByReportedTrue();
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotelId = :hotelId AND r.hidden = false")
    Double findAverageRating(@Param("hotelId") Long hotelId);

    // admin용 통계 메소드들 (단순 필드 기반)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hidden = false")
    Double getAverageRating();
}
