package com.advisor.booking.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class AvailabilityController {

    private final BookingService bookingService;

    public AvailabilityController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/api/availability")
    public ResponseEntity<?> getAvailability(
            @RequestParam Long advisorId,
            @RequestParam String date) {

        log.info("Getting availability for advisor {} on date {}", advisorId, date);

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        List<BookingSlot> slots = bookingService.getAvailableSlots(advisorId, startOfDay);

        List<Map<String, Object>> availableSlots = slots.stream()
            .map(slot -> Map.<String, Object>of(
                "slotId", slot.getId(),
                "startTime", slot.getSlotStartTime(),
                "endTime", slot.getSlotEndTime()
            ))
            .toList();

        return ResponseEntity.ok(Map.of(
            "advisorId", advisorId,
            "date", date,
            "availableSlots", availableSlots,
            "totalSlots", availableSlots.size()
        ));
    }
}
