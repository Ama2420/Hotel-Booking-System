package com.rupeek.hotelbooking.domain.cancellation;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.LocalDateTime;

/**
 * Decides how much of what the guest paid is returned on cancellation. Each property chooses a policy by
 * {@link #code()}, and the code is snapshotted onto every booking.
 */
public interface CancellationPolicy {

    String code();

    Money refundFor(Booking booking, Money amountPaid, LocalDateTime cancelledAt);
}
