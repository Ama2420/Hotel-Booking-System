package com.rupeek.hotelbooking.domain.booking;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;
import static com.rupeek.hotelbooking.domain.common.Validation.requirePositive;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Platform-wide rules for stays and payment holds, shared by search and booking so both reject the same inputs.
 */
public record BookingPolicy(int maxNights, Duration holdDuration) {

    public BookingPolicy {
        requirePositive(maxNights, "maxNights");
        requireNonNull(holdDuration, "holdDuration");
        if (holdDuration.isNegative() || holdDuration.isZero()) {
            throw new IllegalArgumentException("holdDuration must be positive");
        }
    }

    public void validateStay(DateRange stay, LocalDate today) {
        if (stay.checkIn().isBefore(today)) {
            throw new IllegalArgumentException("checkIn must not be in the past");
        }
        if (stay.nightCount() > maxNights) {
            throw new IllegalArgumentException("a stay must not exceed " + maxNights + " nights");
        }
    }

    public Instant holdExpiryFor(Instant heldAt) {
        return heldAt.plus(holdDuration);
    }
}
