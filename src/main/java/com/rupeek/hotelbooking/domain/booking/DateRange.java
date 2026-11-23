package com.rupeek.hotelbooking.domain.booking;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * A stay from check-in (inclusive) to check-out (exclusive). The guest occupies the room on every night
 * from {@code checkIn} up to, but not including, {@code checkOut}.
 */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        requireNonNull(checkIn, "checkIn");
        requireNonNull(checkOut, "checkOut");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }

    public long nightCount() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** The dates whose night is occupied by this stay. */
    public Stream<LocalDate> nights() {
        return checkIn.datesUntil(checkOut);
    }
}
