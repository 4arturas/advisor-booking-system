package com.advisor.booking.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking_slot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"advisor_id", "start_time", "end_time"})
})
public class BookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advisor_id", nullable = false)
    private Long advisorId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime slotStartTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime slotEndTime;

    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;
}
