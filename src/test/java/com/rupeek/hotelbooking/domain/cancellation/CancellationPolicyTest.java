package com.rupeek.hotelbooking.domain.cancellation;

import static org.assertj.core.api.Assertions.assertThat;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingRequest;
import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.booking.Guest;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CancellationPolicyTest {

    private static final Money PAID = Money.of("9000");
    /** Two nights, two rooms at 2250: first night for both rooms is 4500. */
    private static final Booking BOOKING = Booking.hold("bk_1",
            new BookingRequest("prop_1", "rt_1", new Guest("Asha", "asha@example.com"),
                    new DateRange(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)), 4, 2),
            PAID, FlexibleCancellationPolicy.CODE, Instant.parse("2026-12-01T00:00:00Z"),
            Instant.parse("2026-12-01T00:15:00Z"));
    private static final LocalDateTime CHECK_IN = BOOKING.checkInDateTime();

    private final FlexibleCancellationPolicy flexible = new FlexibleCancellationPolicy(Duration.ofHours(24));

    @Test
    void flexibleRefundsInFullUpToTheFreeCancellationDeadline() {
        assertThat(flexible.refundFor(BOOKING, PAID, CHECK_IN.minusDays(5))).isEqualTo(PAID);
        assertThat(flexible.refundFor(BOOKING, PAID, CHECK_IN.minusHours(24))).isEqualTo(PAID);
    }

    @Test
    void flexibleRetainsTheFirstNightAfterTheDeadline() {
        assertThat(flexible.refundFor(BOOKING, PAID, CHECK_IN.minusHours(23))).isEqualTo(Money.of("4500"));
    }

    @Test
    void nothingIsRefundedWhenNothingWasPaid() {
        assertThat(flexible.refundFor(BOOKING, Money.ZERO, CHECK_IN.minusDays(5))).isEqualTo(Money.ZERO);
        assertThat(flexible.refundFor(BOOKING, Money.ZERO, CHECK_IN.minusHours(1))).isEqualTo(Money.ZERO);
    }

    @Test
    void nonRefundableNeverRefunds() {
        assertThat(new NonRefundableCancellationPolicy().refundFor(BOOKING, PAID, CHECK_IN.minusDays(30)))
                .isEqualTo(Money.ZERO);
    }
}
