package com.advisor.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN b.slot s WHERE b.advisorId = :advisorId " +
           "AND s.slotStartTime BETWEEN :start AND :end")
    List<Booking> findByAdvisorIdAndSlotSlotStartTimeBetween(
        @Param("advisorId") Long advisorId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
