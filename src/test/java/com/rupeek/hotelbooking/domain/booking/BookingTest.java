package com.rupeek.hotelbooking.domain.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.common.InvalidStateTransitionException;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingTest {

    private static final Instant NOW = Instant.parse("2026-12-01T06:30:00Z");
    private static final Instant HOLD_EXPIRY = NOW.plusSeconds(900);
    private static final DateRange STAY = new DateRange(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

    private static Booking newBooking() {
        BookingRequest request = new BookingRequest("prop_1", "rt_1", new Guest("Asha", "asha@example.com"),
                STAY, 2, 1);
        return Booking.hold("bk_1", request, Money.of("9000"), "FLEXIBLE", NOW, HOLD_EXPIRY);
    }

    @Test
    void startsAwaitingPayment() {
        assertThat(newBooking().status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmedBookingCanBeCancelledWithRefund() {
        Booking booking = newBooking();
        booking.confirm();
        booking.cancel(Money.of("4500"));

        assertThat(booking.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.refundAmount()).isEqualTo(Money.of("4500"));
    }

    @Test
    void terminalStatesRejectFurtherTransitions() {
        Booking cancelled = newBooking();
        cancelled.cancel(Money.ZERO);
        Booking expired = newBooking();
        expired.expire();

        assertThatThrownBy(cancelled::confirm).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> cancelled.cancel(Money.ZERO)).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(expired::confirm).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void confirmedBookingCannotExpireOrBeConfirmedAgain() {
        Booking booking = newBooking();
        booking.confirm();

        assertThatThrownBy(booking::expire).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(booking::confirm).isInstanceOf(InvalidStateTransitionException.class);
    }

    @ParameterizedTest(name = "{0} -> {1} allowed={2}")
    @CsvSource({
            "PENDING_PAYMENT, CONFIRMED, true",
            "PENDING_PAYMENT, CANCELLED, true",
            "PENDING_PAYMENT, EXPIRED, true",
            "CONFIRMED, CANCELLED, true",
            "CONFIRMED, EXPIRED, false",
            "CONFIRMED, PENDING_PAYMENT, false",
            "CANCELLED, CONFIRMED, false",
            "EXPIRED, CONFIRMED, false",
            "EXPIRED, CANCELLED, false"
    })
    void transitionTable(BookingStatus from, BookingStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @Test
    void holdLapsesAtExpiryOnlyWhileAwaitingPayment() {
        Booking pending = newBooking();
        Booking confirmed = newBooking();
        confirmed.confirm();

        assertThat(pending.isHoldLapsed(HOLD_EXPIRY.minusSeconds(1))).isFalse();
        assertThat(pending.isHoldLapsed(HOLD_EXPIRY)).isTrue();
        assertThat(confirmed.isHoldLapsed(HOLD_EXPIRY.plusSeconds(3600))).isFalse();
    }

    @Test
    void stayStartsAtCheckInTimeOnCheckInDate() {
        Booking booking = newBooking();
        LocalDateTime checkIn = LocalDate.of(2026, 12, 10).atTime(Booking.CHECK_IN_TIME);

        assertThat(booking.hasStayStarted(checkIn.minusMinutes(1))).isFalse();
        assertThat(booking.hasStayStarted(checkIn)).isTrue();
    }
}
