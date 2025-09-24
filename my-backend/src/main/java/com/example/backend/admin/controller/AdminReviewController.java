package com.example.backend.admin.controller;

import com.example.backend.admin.security.AdminGuard;
import com.example.backend.admin.dto.ReviewAdminDto;
import com.example.backend.review.domain.Review;
import com.example.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminReviewController {
    private final AdminGuard adminGuard;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<Page<ReviewAdminDto>> list(Authentication authentication,
                                             @RequestParam(required = false, defaultValue = "false") boolean includeHidden,
                                             @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        Page<Review> page = includeHidden ? reviewRepository.findAll(pageable) : reviewRepository.findByHiddenFalse(pageable);
        Page<ReviewAdminDto> dtoPage = new PageImpl<>(
                page.getContent().stream().map(ReviewAdminDto::from).toList(),
                pageable,
                page.getTotalElements()
        );
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        return reviewRepository.findById(id)
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(ReviewAdminDto.from(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/hide")
    public ResponseEntity<?> hide(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        return reviewRepository.findById(id)
                .<ResponseEntity<?>>map(r -> { r.setHidden(true); return ResponseEntity.ok(ReviewAdminDto.from(reviewRepository.save(r))); })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/show")
    public ResponseEntity<?> show(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        return reviewRepository.findById(id)
                .<ResponseEntity<?>>map(r -> { r.setHidden(false); return ResponseEntity.ok(ReviewAdminDto.from(reviewRepository.save(r))); })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/report")
    public ResponseEntity<?> report(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        return reviewRepository.findById(id)
                .<ResponseEntity<?>>map(r -> { r.setReported(true); return ResponseEntity.ok(ReviewAdminDto.from(reviewRepository.save(r))); })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
