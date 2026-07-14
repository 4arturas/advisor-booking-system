package com.advisor.booking.booking;

import com.advisor.booking.calendar.CalendarService;
import com.advisor.booking.notification.NotificationService;
import com.advisor.booking.notification.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final CalendarService calendarService;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository, BookingSlotRepository bookingSlotRepository,
                          CalendarService calendarService, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.bookingSlotRepository = bookingSlotRepository;
        this.calendarService = calendarService;
        this.notificationService = notificationService;
    }

    public List<BookingSlot> getAvailableSlots(Long advisorId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        
        return bookingSlotRepository
            .findByAdvisorIdAndIsBookedFalseAndSlotStartTimeBetween(advisorId, startOfDay, endOfDay);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {
        log.info("Creating booking for advisor {} slot {}", request.advisorId(), request.slotId());
        
        BookingSlot slot = bookingSlotRepository
            .findAndLockAvailableSlotById(request.slotId())
            .orElseThrow(() -> new SlotNotAvailableException(
                "Slot not available: " + request.slotId()
            ));

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setAdvisorId(request.advisorId());
        booking.setCustomerName(request.customerName());
        booking.setCustomerEmail(request.customerEmail());

        slot.setIsBooked(true);
        bookingSlotRepository.save(slot);

        Booking savedBooking = bookingRepository.save(booking);
        
        log.info("Booking created with ID: {}", savedBooking.getId());

        try {
            String eventId = calendarService.createCalendarEvent(
                request.advisorId(),
                request.customerName(),
                slot.getSlotStartTime(),
                slot.getSlotEndTime()
            );
            savedBooking.setGraphEventId(eventId);
            savedBooking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(savedBooking);
            
            log.info("Calendar event created for booking {}", savedBooking.getId());
        } catch (Exception e) {
            log.error("Failed to create calendar event for booking {}: {}", 
                savedBooking.getId(), e.getMessage());
        }

        notificationService.sendBookingConfirmationEmail(new NotificationEvent(
            savedBooking.getId(),
            savedBooking.getAdvisorId(),
            savedBooking.getCustomerName(),
            savedBooking.getCustomerEmail(),
            slot.getSlotStartTime(),
            slot.getSlotEndTime()
        ));

        log.info("Booking {} confirmed and notification handled", savedBooking.getId());
        return savedBooking;
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
    }

    public List<Booking> getBookingsByAdvisor(Long advisorId, LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByAdvisorIdAndSlotSlotStartTimeBetween(advisorId, startDate, endDate);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);

        bookingSlotRepository.findById(booking.getSlotId()).ifPresent(slot -> {
            slot.setIsBooked(false);
            bookingSlotRepository.save(slot);
        });

        bookingRepository.delete(booking);

        log.info("Booking {} cancelled", bookingId);
        return booking;
    }

    public record CreateBookingRequest(
        Long advisorId,
        Long slotId,
        String customerName,
        String customerEmail
    ) {}

    public static class SlotNotAvailableException extends RuntimeException {
        public SlotNotAvailableException(String message) {
            super(message);
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) {
            super(message);
        }
    }

    public static class BookingAlreadyCancelledException extends RuntimeException {
        public BookingAlreadyCancelledException(String message) {
            super(message);
        }
    }
}
