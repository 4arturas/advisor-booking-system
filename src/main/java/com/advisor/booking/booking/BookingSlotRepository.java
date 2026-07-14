package com.advisor.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BookingSlot s WHERE s.id = :slotId AND s.isBooked = false")
    Optional<BookingSlot> findAndLockAvailableSlotById(@Param("slotId") Long slotId);

    List<BookingSlot> findByAdvisorIdAndIsBookedFalseAndSlotStartTimeBetween(
        Long advisorId, 
        LocalDateTime start, 
        LocalDateTime end
    );
}
