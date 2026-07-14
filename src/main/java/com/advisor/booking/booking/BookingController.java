package com.advisor.booking.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/availability/{advisorId}")
    public ResponseEntity<List<BookingSlot>> getAvailableSlots(
            @PathVariable Long advisorId,
            @RequestParam String date) {
        
        log.info("Getting available slots for advisor {} on date {}", advisorId, date);
        
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        List<BookingSlot> slots = bookingService.getAvailableSlots(advisorId, dateTime);
        
        return ResponseEntity.ok(slots);
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingService.CreateBookingRequest request) {
        try {
            log.info("Creating booking for advisor {} slot {}", 
                request.advisorId(), request.slotId());
            
            Booking booking = bookingService.createBooking(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", booking.getId(),
                "status", booking.getStatus().name(),
                "advisorId", booking.getAdvisorId(),
                "slotId", booking.getSlotId(),
                "slotStartTime", booking.getSlot().getSlotStartTime(),
                "slotEndTime", booking.getSlot().getSlotEndTime(),
                "customerName", booking.getCustomerName(),
                "customerEmail", booking.getCustomerEmail()
            ));
        } catch (BookingService.SlotNotAvailableException e) {
            log.warn("Slot not available: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Slot not available",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating booking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.getBooking(bookingId);
            return ResponseEntity.ok(Map.of(
                "id", booking.getId(),
                "status", booking.getStatus().name(),
                "advisorId", booking.getAdvisorId(),
                "slotId", booking.getSlotId(),
                "slotStartTime", booking.getSlot().getSlotStartTime(),
                "slotEndTime", booking.getSlot().getSlotEndTime(),
                "customerName", booking.getCustomerName(),
                "customerEmail", booking.getCustomerEmail(),
                "createdAt", booking.getCreatedAt()
            ));
        } catch (BookingService.BookingNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Booking not found",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/advisor/{advisorId}")
    public ResponseEntity<List<Booking>> getBookingsByAdvisor(
            @PathVariable Long advisorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
        
        List<Booking> bookings = bookingService.getBookingsByAdvisor(advisorId, start, end);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(Map.of(
                "id", booking.getId(),
                "status", Booking.BookingStatus.CANCELLED.name(),
                "message", "Booking cancelled successfully"
            ));
        } catch (BookingService.BookingNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Booking not found",
                "message", e.getMessage()
            ));
        } catch (BookingService.BookingAlreadyCancelledException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Booking already cancelled",
                "message", e.getMessage()
            ));
        }
    }
}
