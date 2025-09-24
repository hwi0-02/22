package com.example.backend.hotel_reservation.repository;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 기존 메소드들
    List<Reservation> findTop500ByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);
    long countByStartDateBetween(Instant from, Instant to);
    long countByRoomIdIn(List<Long> roomIds);
    long countByCreatedAtBetween(Instant from, Instant to);

    long countByStatus(ReservationStatus status);
    Integer countByRoomId(Long roomId);
}
