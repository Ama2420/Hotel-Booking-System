package com.rupeek.hotelbooking.domain.cancellation;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Full refund when cancelled at least {@code freeCancellationWindow} before check-in; after that, the first
 * night (for all booked rooms) is retained and the rest refunded.
 */
public class FlexibleCancellationPolicy implements CancellationPolicy {

    public static final String CODE = "FLEXIBLE";

    private final Duration freeCancellationWindow;

    public FlexibleCancellationPolicy(Duration freeCancellationWindow) {
        this.freeCancellationWindow = requireNonNull(freeCancellationWindow, "freeCancellationWindow");
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Money refundFor(Booking booking, Money amountPaid, LocalDateTime cancelledAt) {
        LocalDateTime freeCancellationDeadline = booking.checkInDateTime().minus(freeCancellationWindow);
        if (!cancelledAt.isAfter(freeCancellationDeadline)) {
            return amountPaid;
        }
        Money firstNight = booking.totalPrice().dividedBy(booking.stay().nightCount());
        return amountPaid.minusFloorZero(firstNight);
    }
}
