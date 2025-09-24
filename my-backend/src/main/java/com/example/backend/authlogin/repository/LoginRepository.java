package com.example.backend.authlogin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.backend.authlogin.domain.User;

public interface LoginRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    List<User> findAllByEmail(String email); // 중복 계정 조회용
    boolean existsByEmail(String email);
    
    // 간소화된 사용자 조회 메서드 (소셜 로그인 관련 제거)

    @Query("SELECT u FROM User u WHERE (:name IS NULL OR u.name LIKE %:name%) AND (:email IS NULL OR u.email LIKE %:email%) AND (:role IS NULL OR u.role = :role)")
    Page<User> findUsersWithFilters(@Param("name") String name,
                                   @Param("email") String email,
                                   @Param("role") User.Role role,
                                   Pageable pageable);
    
    // 관리자 통계용 메서드들
    Long countByRole(User.Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdOn >= :startDate")
    Long getNewUsersCount(@Param("startDate") java.time.LocalDateTime startDate);
}