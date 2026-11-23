package com.rupeek.hotelbooking.domain.booking;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import com.rupeek.hotelbooking.domain.common.InvalidStateTransitionException;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A reservation of {@code roomCount} units of one room type for a stay. Inventory is held from the moment the
 * booking is created (PENDING_PAYMENT) until it is cancelled or its payment hold lapses.
 *
 * <p>The price and cancellation policy are captured at booking time so later changes to the property do not
 * alter what the guest agreed to. All state changes go through {@link BookingStatus}'s transition table.
 */
public class Booking {

    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);

    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final Guest guest;
    private final DateRange stay;
    private final int guestCount;
    private final int roomCount;
    private final Money totalPrice;
    private final String cancellationPolicyCode;
    private final Instant createdAt;
    private final Instant holdExpiresAt;
    private volatile BookingStatus status;
    private volatile Money refundAmount = Money.ZERO;

    private Booking(String id, BookingRequest request, Money totalPrice, String cancellationPolicyCode,
                    Instant createdAt, Instant holdExpiresAt) {
        this.id = requireNonBlank(id, "booking.id");
        this.propertyId = request.propertyId();
        this.roomTypeId = request.roomTypeId();
        this.guest = request.guest();
        this.stay = request.stay();
        this.guestCount = request.guestCount();
        this.roomCount = request.roomCount();
        this.totalPrice = requireNonNull(totalPrice, "totalPrice");
        this.cancellationPolicyCode = requireNonBlank(cancellationPolicyCode, "cancellationPolicy");
        this.createdAt = requireNonNull(createdAt, "createdAt");
        this.holdExpiresAt = requireNonNull(holdExpiresAt, "holdExpiresAt");
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    public static Booking hold(String id, BookingRequest request, Money totalPrice, String cancellationPolicyCode,
                               Instant createdAt, Instant holdExpiresAt) {
        requireNonNull(request, "request");
        return new Booking(id, request, totalPrice, cancellationPolicyCode, createdAt, holdExpiresAt);
    }

    public void confirm() {
        transitionTo(BookingStatus.CONFIRMED);
    }

    public void expire() {
        transitionTo(BookingStatus.EXPIRED);
    }

    public void cancel(Money refund) {
        requireNonNull(refund, "refund");
        transitionTo(BookingStatus.CANCELLED);
        this.refundAmount = refund;
    }

    /** Lets callers fail fast before side effects (e.g. charging a card) that precede the actual transition. */
    public void assertCanTransitionTo(BookingStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException("Booking", id, status, target);
        }
    }

    public boolean isHoldLapsed(Instant now) {
        return status == BookingStatus.PENDING_PAYMENT && !now.isBefore(holdExpiresAt);
    }

    public boolean hasStayStarted(LocalDateTime now) {
        return !now.isBefore(checkInDateTime());
    }

    public LocalDateTime checkInDateTime() {
        return stay.checkIn().atTime(CHECK_IN_TIME);
    }

    private void transitionTo(BookingStatus target) {
        assertCanTransitionTo(target);
        this.status = target;
    }

    public String id() {
        return id;
    }

    public String propertyId() {
        return propertyId;
    }

    public String roomTypeId() {
        return roomTypeId;
    }

    public Guest guest() {
        return guest;
    }

    public DateRange stay() {
        return stay;
    }

    public int guestCount() {
        return guestCount;
    }

    public int roomCount() {
        return roomCount;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public String cancellationPolicyCode() {
        return cancellationPolicyCode;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant holdExpiresAt() {
        return holdExpiresAt;
    }

    public BookingStatus status() {
        return status;
    }

    public Money refundAmount() {
        return refundAmount;
    }
}
