package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.GUEST;
import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.bookingFor;
import static com.rupeek.hotelbooking.support.TestData.hotelWith;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingRequest;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.inventory.InsufficientInventoryException;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.support.HotelTestContext;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private final HotelTestContext ctx = new HotelTestContext();
    private final Property hotel = ctx.onboard(hotelWith("FLEXIBLE", roomType("Deluxe", 2, 2, "3000")));
    private final String roomTypeId = hotel.roomTypes().get(0).id();

    @Test
    void createsAPricedBookingThatHoldsInventoryUntilPaid() {
        Booking booking = ctx.bookingService.createBooking(bookingFor(hotel, STAY, 3, 2));

        assertThat(booking.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.totalPrice()).isEqualTo(Money.of("12000"));
        assertThat(booking.holdExpiresAt()).isEqualTo(HotelTestContext.START.plus(HotelTestContext.HOLD));
        assertThat(booking.cancellationPolicyCode()).isEqualTo("FLEXIBLE");
        assertThat(ctx.inventoryService.availableUnits(roomTypeId, STAY)).isZero();
    }

    @Test
    void preventsDoubleBookingOfTheSameInventory() {
        ctx.bookingService.createBooking(bookingFor(hotel, STAY, 2, 2));
        DateRange overlapping = new DateRange(STAY.checkIn().plusDays(1), STAY.checkOut().plusDays(1));

        assertThatThrownBy(() -> ctx.bookingService.createBooking(bookingFor(hotel, overlapping, 2, 1)))
                .isInstanceOf(InsufficientInventoryException.class);
    }

    @Test
    void rejectsPartiesTooLargeForTheRoomsRequested() {
        assertThatThrownBy(() -> ctx.bookingService.createBooking(bookingFor(hotel, STAY, 3, 1)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("sleep at most 2");
        assertThat(ctx.inventoryService.availableUnits(roomTypeId, STAY)).isEqualTo(2);
    }

    @Test
    void rejectsUnknownPropertyOrRoomType() {
        BookingRequest unknownRoom = new BookingRequest(hotel.id(), "rt_missing", GUEST, STAY, 2, 1);
        BookingRequest unknownProperty = new BookingRequest("prop_missing", roomTypeId, GUEST, STAY, 2, 1);

        assertThatThrownBy(() -> ctx.bookingService.createBooking(unknownRoom))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> ctx.bookingService.createBooking(unknownProperty))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsStaysInThePastOrLongerThanTheMaximum() {
        DateRange past = new DateRange(LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 2));
        DateRange tooLong = new DateRange(STAY.checkIn(), STAY.checkIn().plusDays(31));

        assertThatThrownBy(() -> ctx.bookingService.createBooking(bookingFor(hotel, past, 2, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ctx.bookingService.createBooking(bookingFor(hotel, tooLong, 2, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lapsedHoldsAreExpiredAndTheirRoomsReleased() {
        Booking booking = ctx.bookingService.createBooking(bookingFor(hotel, STAY, 2, 2));

        ctx.clock.advance(HotelTestContext.HOLD.minus(Duration.ofSeconds(1)));
        assertThat(ctx.bookingService.expireLapsedHolds()).isZero();

        ctx.clock.advance(Duration.ofSeconds(1));
        assertThat(ctx.bookingService.expireLapsedHolds()).isEqualTo(1);
        assertThat(ctx.bookingService.getBooking(booking.id()).status()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(ctx.inventoryService.availableUnits(roomTypeId, STAY)).isEqualTo(2);
    }
}
