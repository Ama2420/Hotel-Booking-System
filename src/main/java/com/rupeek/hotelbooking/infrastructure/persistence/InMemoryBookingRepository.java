package com.rupeek.hotelbooking.infrastructure.persistence;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookingRepository extends InMemoryRepository<Booking> implements BookingRepository {

    public InMemoryBookingRepository() {
        super(Booking::id);
    }

    @Override
    public List<Booking> findLapsedHolds(Instant now) {
        return all().filter(booking -> booking.isHoldLapsed(now)).toList();
    }
}
