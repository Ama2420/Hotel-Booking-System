package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.bookingFor;
import static com.rupeek.hotelbooking.support.TestData.hotelWith;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.InvalidStateTransitionException;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.UpiInstrument;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.support.HotelTestContext;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CancellationServiceTest {

    private final HotelTestContext ctx = new HotelTestContext();

    private Booking paidBooking(String policy) {
        Property hotel = ctx.onboard(hotelWith(policy, roomType("Deluxe", 2, 1, "3000")));
        Booking booking = ctx.bookingService.createBooking(bookingFor(hotel));
        ctx.paymentService.pay(booking.id(), "key-1", new UpiInstrument("asha@okaxis"));
        return booking;
    }

    private void moveClockToHoursBeforeCheckIn(Booking booking, int hours) {
        ctx.clock.setTo(booking.checkInDateTime().minusHours(hours).toInstant(ZoneOffset.UTC));
    }

    @Test
    void earlyCancellationUnderFlexiblePolicyRefundsInFullAndReleasesTheRoom() {
        Booking booking = paidBooking("FLEXIBLE");

        Booking cancelled = ctx.cancellationService.cancel(booking.id());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.refundAmount()).isEqualTo(Money.of("6000"));
        assertThat(ctx.gateway.refunds()).isEqualTo(1);
        assertThat(ctx.paymentRepository.findSuccessfulByBookingId(booking.id()).orElseThrow().refundedAmount())
                .isEqualTo(Money.of("6000"));
        assertThat(ctx.inventoryService.availableUnits(booking.roomTypeId(), STAY)).isEqualTo(1);
    }

    @Test
    void lateCancellationUnderFlexiblePolicyRetainsTheFirstNight() {
        Booking booking = paidBooking("FLEXIBLE");
        moveClockToHoursBeforeCheckIn(booking, 10);

        assertThat(ctx.cancellationService.cancel(booking.id()).refundAmount()).isEqualTo(Money.of("3000"));
    }

    @Test
    void nonRefundableCancellationRefundsNothingButStillReleasesTheRoom() {
        Booking booking = paidBooking("NON_REFUNDABLE");

        Booking cancelled = ctx.cancellationService.cancel(booking.id());

        assertThat(cancelled.refundAmount()).isEqualTo(Money.ZERO);
        assertThat(ctx.gateway.refunds()).isZero();
        assertThat(ctx.inventoryService.availableUnits(booking.roomTypeId(), STAY)).isEqualTo(1);
    }

    @Test
    void unpaidBookingCanBeCancelledWithoutARefund() {
        Property hotel = ctx.onboard(hotelWith("FLEXIBLE", roomType("Deluxe", 2, 1, "3000")));
        Booking booking = ctx.bookingService.createBooking(bookingFor(hotel));

        Booking cancelled = ctx.cancellationService.cancel(booking.id());

        assertThat(cancelled.refundAmount()).isEqualTo(Money.ZERO);
        assertThat(ctx.gateway.refunds()).isZero();
        assertThat(ctx.inventoryService.availableUnits(booking.roomTypeId(), STAY)).isEqualTo(1);
    }

    @Test
    void cancellingTwiceIsRejectedAndDoesNotReleaseInventoryTwice() {
        Booking booking = paidBooking("FLEXIBLE");
        ctx.cancellationService.cancel(booking.id());

        assertThatThrownBy(() -> ctx.cancellationService.cancel(booking.id()))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(ctx.gateway.refunds()).isEqualTo(1);
    }

    @Test
    void cannotCancelOnceTheStayHasStarted() {
        Booking booking = paidBooking("FLEXIBLE");
        moveClockToHoursBeforeCheckIn(booking, 0);

        assertThatThrownBy(() -> ctx.cancellationService.cancel(booking.id()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(ctx.bookingService.getBooking(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
