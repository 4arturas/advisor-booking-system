package com.advisor.booking.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private Long bookingId;
    private Long advisorId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private String notificationType;
    private LocalDateTime timestamp;

    public NotificationEvent(Long bookingId, Long advisorId, String customerName, 
                           String customerEmail, LocalDateTime slotStartTime, LocalDateTime slotEndTime) {
        this.bookingId = bookingId;
        this.advisorId = advisorId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
        this.notificationType = "BOOKING_CONFIRMED";
        this.timestamp = LocalDateTime.now();
    }
}
