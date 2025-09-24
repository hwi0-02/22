package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Business;
import com.example.backend.authlogin.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUser(User user);

    boolean existsByBusinessNumber(String businessNumber);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT b FROM Business b WHERE (:status IS NULL OR b.status = :status OR (:includeNull = TRUE AND b.status IS NULL))")
    Page<Business> findBusinessesWithFilters(@Param("status") Business.BusinessStatus status,
                                             @Param("includeNull") boolean includeNull,
                                             Pageable pageable);

    long countByStatus(Business.BusinessStatus status);

    @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m'), COUNT(b) FROM Business b WHERE b.createdAt BETWEEN :from AND :to GROUP BY FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') ORDER BY FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m')")
    List<Object[]> getMonthlySignupsByDateRange(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    @Query("SELECT b.createdAt FROM Business b WHERE b.createdAt BETWEEN :from AND :to")
    List<LocalDateTime> findCreationTimesByDateRange(@Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);
}
