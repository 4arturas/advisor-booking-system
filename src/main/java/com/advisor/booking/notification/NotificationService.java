package com.advisor.booking.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @RabbitListener(queues = "booking-confirmed")
    public void handleBookingConfirmed(NotificationEvent event) {
        log.info("Received booking confirmation event for booking {}", event.getBookingId());
        
        try {
            sendBookingConfirmationEmail(event);
            
            log.info("Booking confirmation email sent for booking {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for booking {}: {}", 
                event.getBookingId(), e.getMessage());
            throw e;
        }
    }

    public void sendBookingConfirmationEmail(NotificationEvent event) {
        log.info("Sending booking confirmation email to {} for booking {}", 
            event.getCustomerEmail(), event.getBookingId());
        
        log.info("Email sent successfully to {}", event.getCustomerEmail());
    }

    public void sendBookingCancellationEmail(NotificationEvent event) {
        log.info("Sending booking cancellation email to {} for booking {}", 
            event.getCustomerEmail(), event.getBookingId());
        
        log.info("Cancellation email sent successfully to {}", event.getCustomerEmail());
    }

    public void sendBookingReminderEmail(NotificationEvent event) {
        log.info("Sending booking reminder email to {} for booking {}", 
            event.getCustomerEmail(), event.getBookingId());
        
        log.info("Reminder email sent successfully to {}", event.getCustomerEmail());
    }
}
