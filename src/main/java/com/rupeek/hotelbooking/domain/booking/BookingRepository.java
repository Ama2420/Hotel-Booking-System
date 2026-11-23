package com.rupeek.hotelbooking.domain.booking;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String id);

    /** Bookings still awaiting payment whose hold has lapsed at {@code now}. */
    List<Booking> findLapsedHolds(Instant now);
}
