package com.rupeek.hotelbooking.domain.cancellation;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.LocalDateTime;

public class NonRefundableCancellationPolicy implements CancellationPolicy {

    public static final String CODE = "NON_REFUNDABLE";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Money refundFor(Booking booking, Money amountPaid, LocalDateTime cancelledAt) {
        return Money.ZERO;
    }
}
