package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.bookingFor;
import static com.rupeek.hotelbooking.support.TestData.hotelWith;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingHoldExpiredException;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.common.InvalidStateTransitionException;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.CardInstrument;
import com.rupeek.hotelbooking.domain.payment.IdempotencyKeyConflictException;
import com.rupeek.hotelbooking.domain.payment.Payment;
import com.rupeek.hotelbooking.domain.payment.PaymentInstrument;
import com.rupeek.hotelbooking.domain.payment.PaymentStatus;
import com.rupeek.hotelbooking.domain.payment.UpiInstrument;
import com.rupeek.hotelbooking.domain.payment.WalletInstrument;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.support.HotelTestContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PaymentServiceTest {

    private static final UpiInstrument UPI = new UpiInstrument("asha@okaxis");
    private static final UpiInstrument DECLINING_UPI = new UpiInstrument("fail@okaxis");

    private final HotelTestContext ctx = new HotelTestContext();
    private final Property hotel = ctx.onboard(hotelWith("FLEXIBLE", roomType("Deluxe", 2, 1, "3000")));
    private final Booking booking = ctx.bookingService.createBooking(bookingFor(hotel));

    static Stream<PaymentInstrument> everySupportedMethod() {
        return Stream.of(
                new CardInstrument("4111 1111 1111 1111", "Asha Rao"),
                new UpiInstrument("asha@okaxis"),
                new WalletInstrument("paytm", "9876543210"));
    }

    @ParameterizedTest
    @MethodSource("everySupportedMethod")
    void successfulPaymentConfirmsTheBooking(PaymentInstrument instrument) {
        Payment payment = ctx.paymentService.pay(booking.id(), "key-1", instrument);

        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.amount()).isEqualTo(Money.of("6000"));
        assertThat(payment.method()).isEqualTo(instrument.method());
        assertThat(status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void declinedPaymentKeepsTheBookingPayableSoTheGuestCanRetry() {
        Payment declined = ctx.paymentService.pay(booking.id(), "key-1", DECLINING_UPI);

        assertThat(declined.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(declined.failureReason()).isNotBlank();
        assertThat(status()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        Payment retried = ctx.paymentService.pay(booking.id(), "key-2", UPI);
        assertThat(retried.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void retryingWithTheSameKeyReturnsTheOriginalPaymentWithoutChargingAgain() {
        Payment first = ctx.paymentService.pay(booking.id(), "key-1", UPI);
        Payment replay = ctx.paymentService.pay(booking.id(), "key-1", UPI);

        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(ctx.gateway.charges()).isEqualTo(1);
    }

    @Test
    void replayOfADeclinedAttemptReturnsTheDeclineRatherThanChargingAgain() {
        Payment first = ctx.paymentService.pay(booking.id(), "key-1", DECLINING_UPI);
        Payment replay = ctx.paymentService.pay(booking.id(), "key-1", DECLINING_UPI);

        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(ctx.gateway.charges()).isEqualTo(1);
    }

    @Test
    void reusingAKeyForADifferentPaymentMethodIsAConflict() {
        ctx.paymentService.pay(booking.id(), "key-1", UPI);

        assertThatThrownBy(() -> ctx.paymentService.pay(booking.id(), "key-1",
                new WalletInstrument("paytm", "9876543210")))
                .isInstanceOf(IdempotencyKeyConflictException.class);
    }

    @Test
    void cannotPayTwiceForTheSameBooking() {
        ctx.paymentService.pay(booking.id(), "key-1", UPI);

        assertThatThrownBy(() -> ctx.paymentService.pay(booking.id(), "key-2", UPI))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(ctx.gateway.charges()).isEqualTo(1);
    }

    @Test
    void payingAfterTheHoldLapsesExpiresTheBookingAndReleasesTheRoom() {
        ctx.clock.advance(HotelTestContext.HOLD);

        assertThatThrownBy(() -> ctx.paymentService.pay(booking.id(), "key-1", UPI))
                .isInstanceOf(BookingHoldExpiredException.class);
        assertThat(status()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(ctx.inventoryService.availableUnits(booking.roomTypeId(), STAY)).isEqualTo(1);
        assertThat(ctx.gateway.charges()).isZero();
    }

    @Test
    void cannotPayForACancelledBooking() {
        ctx.cancellationService.cancel(booking.id());

        assertThatThrownBy(() -> ctx.paymentService.pay(booking.id(), "key-1", UPI))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(ctx.gateway.charges()).isZero();
    }

    @Test
    void requiresAnIdempotencyKey() {
        assertThatThrownBy(() -> ctx.paymentService.pay(booking.id(), " ", UPI))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private BookingStatus status() {
        return ctx.bookingService.getBooking(booking.id()).status();
    }
}
