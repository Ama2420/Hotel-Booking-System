package com.rupeek.hotelbooking.infrastructure.scheduling;

import com.rupeek.hotelbooking.application.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically releases inventory held by unpaid bookings. Holds are also expired lazily when someone tries to
 * pay, so correctness does not depend on this job's timing, only how soon rooms reappear in search.
 */
@Component
public class HoldExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryScheduler.class);

    private final BookingService bookingService;

    public HoldExpiryScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelayString = "${hotel.booking.hold-sweep-interval}")
    public void expireLapsedHolds() {
        int expired = bookingService.expireLapsedHolds();
        if (expired > 0) {
            log.info("Expired {} booking(s) with lapsed payment holds", expired);
        }
    }
}
