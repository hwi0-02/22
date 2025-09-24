package com.example.backend.admin.controller;

import com.example.backend.hotel_reservation.domain.Room;
import com.example.backend.hotel_reservation.domain.RoomInventory;
import com.example.backend.hotel_reservation.repository.RoomInventoryRepository;
import com.example.backend.hotel_reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.backend.admin.security.AdminGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminRoomController {

    private final RoomRepository roomRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final AdminGuard adminGuard;

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(Authentication authentication,
                                                          @RequestParam(required = false) Long hotelId,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        List<Room> rooms = hotelId != null ? roomRepository.findByHotelId(hotelId) : roomRepository.findAll();
        List<Map<String, Object>> mapped = rooms.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("hotelId", r.getHotelId());
            m.put("name", r.getName());
            m.put("roomSize", r.getRoomSize());
            m.put("price", r.getPrice());
            return m;
        }).collect(Collectors.toList());
        Page<Map<String, Object>> page = new PageImpl<>(mapped, pageable, mapped.size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
    return roomRepository.findById(id)
        .<ResponseEntity<?>>map(r -> {
            java.util.HashMap<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("hotelId", r.getHotelId());
            m.put("name", r.getName());
            m.put("roomSize", r.getRoomSize());
            m.put("price", r.getPrice());
            return ResponseEntity.ok(m);
        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminGuard.requireAdmin(authentication);
        String status = body != null ? String.valueOf(body.get("status")) : null;
        if (status == null) return ResponseEntity.badRequest().body(Map.of("message","status required"));
        return roomRepository.findById(id)
                .<ResponseEntity<?>>map(r -> {
                    r.setStatus(Room.RoomStatus.valueOf(status));
                    roomRepository.save(r);
                    return ResponseEntity.ok(Map.of("id", r.getId(), "status", r.getStatus().name()));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "deleted", "id", id));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<?> inventory(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        // 최근 30일 인벤토리 조회
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from = today.minusDays(30);
        java.util.List<RoomInventory> list = new java.util.ArrayList<>();
        for (int i=0;i<=30;i++) {
            java.time.LocalDate d = from.plusDays(i);
            roomInventoryRepository.findByRoomIdAndDate(id, d).ifPresent(list::add);
        }
        java.util.List<java.util.Map<String,Object>> out = list.stream().map(ri -> {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("date", ri.getDate());
            m.put("totalQuantity", ri.getTotalQuantity());
            m.put("availableQuantity", ri.getAvailableQuantity());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(out);
    }
}
