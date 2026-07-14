package com.advisor.booking.booking;

import com.advisor.booking.advisor.Advisor;
import com.advisor.booking.advisor.AdvisorRepository;
import com.advisor.booking.calendar.CalendarService;
import com.advisor.booking.notification.NotificationEvent;
import com.advisor.booking.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSlotRepository bookingSlotRepository;

    @Autowired
    private TestCalendarService calendarService;

    @Autowired
    private RecordingNotificationService notificationService;

    @AfterEach
    void cleanup() {
        notificationService.events.clear();
    }

    @Test
    void shouldPreventDoubleBookingWhenTwoRequestsCompeteForSameSlot() throws Exception {
        Advisor advisor = advisorRepository.save(new Advisor(null, "Advisor R", uniqueEmail("race")));
        BookingSlot slot = bookingSlotRepository.save(new BookingSlot(
            null,
            advisor.getId(),
            LocalDateTime.of(2026, 7, 14, 10, 0),
            LocalDateTime.of(2026, 7, 14, 11, 0),
            false
        ));

        calendarService.setNextEventId("event-race");

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Outcome> first = bookingAttempt(ready, start, advisor.getId(), slot.getId(), "Alice");
        Callable<Outcome> second = bookingAttempt(ready, start, advisor.getId(), slot.getId(), "Bob");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Outcome> firstResult = executor.submit(first);
            Future<Outcome> secondResult = executor.submit(second);

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            Outcome resultA = firstResult.get(30, TimeUnit.SECONDS);
            Outcome resultB = secondResult.get(30, TimeUnit.SECONDS);

            List<Outcome> outcomes = List.of(resultA, resultB);
            assertThat(outcomes).filteredOn(Outcome::success).hasSize(1);
            assertThat(outcomes).filteredOn(o -> !o.success()).hasSize(1);
            assertThat(outcomes).anySatisfy(outcome ->
                assertThat(outcome.failure).isInstanceOf(BookingService.SlotNotAvailableException.class));

            assertThat(bookingRepository.count()).isEqualTo(1);
            assertThat(bookingSlotRepository.findById(slot.getId()).orElseThrow().getIsBooked()).isTrue();
            assertThat(notificationService.events).hasSize(1);
            assertThat(notificationService.events.get(0).getCustomerName()).isEqualTo("Alice");
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Outcome> bookingAttempt(
        CountDownLatch ready,
        CountDownLatch start,
        Long advisorId,
        Long slotId,
        String customerName
    ) {
        return () -> {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            try {
                Booking booking = bookingService.createBooking(new BookingService.CreateBookingRequest(
                    advisorId,
                    slotId,
                    customerName,
                    customerName.toLowerCase() + "@example.com"
                ));
                return Outcome.success(booking);
            } catch (Exception e) {
                return Outcome.failure(e);
            }
        };
    }

    private String uniqueEmail(String suffix) {
        return suffix + "-" + System.nanoTime() + "@example.com";
    }

    private record Outcome(Booking booking, Throwable failure) {
        static Outcome success(Booking booking) {
            return new Outcome(booking, null);
        }

        static Outcome failure(Throwable failure) {
            return new Outcome(null, failure);
        }

        boolean success() {
            return booking != null;
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Fakes {

        @Bean
        @Primary
        TestCalendarService testCalendarService() {
            return new TestCalendarService();
        }

        @Bean
        @Primary
        RecordingNotificationService testNotificationService() {
            return new RecordingNotificationService();
        }

        @Bean
        @Primary
        ConnectionFactory testConnectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }

    static final class TestCalendarService extends CalendarService {
        private String nextEventId = "event-default";

        @Override
        public String createCalendarEvent(Long advisorId, String customerName, LocalDateTime startTime, LocalDateTime endTime) {
            return nextEventId;
        }

        void setNextEventId(String eventId) {
            this.nextEventId = eventId;
        }
    }

    static final class RecordingNotificationService extends NotificationService {
        private final List<NotificationEvent> events = new ArrayList<>();

        @Override
        public void sendBookingConfirmationEmail(NotificationEvent event) {
            events.add(event);
        }
    }
}
