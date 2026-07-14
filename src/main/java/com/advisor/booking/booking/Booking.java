package com.advisor.booking.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false, unique = true, updatable = false)
    private BookingSlot slot;

    @Column(name = "advisor_id", nullable = false)
    private Long advisorId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "graph_event_id")
    private String graphEventId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private BookingStatus status;

    @PostLoad
    private void initializeTransientStatus() {
        this.status = graphEventId == null ? BookingStatus.PENDING : BookingStatus.CONFIRMED;
    }

    public BookingStatus getStatus() {
        if (status != null) {
            return status;
        }
        return graphEventId == null ? BookingStatus.PENDING : BookingStatus.CONFIRMED;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Long getSlotId() {
        return slot != null ? slot.getId() : null;
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, FAILED
    }
}
