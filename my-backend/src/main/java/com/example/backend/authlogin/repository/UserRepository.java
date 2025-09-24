package com.example.backend.authlogin.repository;

import com.example.backend.authlogin.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR u.name LIKE %:name%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> findUsersWithFilters(@Param("name") String name, 
                                   @Param("email") String email, 
                                   @Param("role") User.Role role, 
                                   Pageable pageable);
    
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdOn, '%Y-%m'), COUNT(u) " +
           "FROM User u " +
           "WHERE u.createdOn BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdOn, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', u.createdOn, '%Y-%m')")
    List<Object[]> getMonthlySignupsByDateRange(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("SELECT u.createdOn FROM User u WHERE u.createdOn BETWEEN :from AND :to")
    List<LocalDateTime> findCreationTimesByDateRange(@Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);
}