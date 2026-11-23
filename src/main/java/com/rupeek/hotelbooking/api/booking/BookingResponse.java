package com.rupeek.hotelbooking.api.booking;

import com.rupeek.hotelbooking.api.booking.CreateBookingRequest.GuestDto;
import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        String id,
        String propertyId,
        String roomTypeId,
        GuestDto guest,
        LocalDate checkIn,
        LocalDate checkOut,
        long nights,
        int guests,
        int rooms,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant holdExpiresAt,
        String cancellationPolicy,
        BigDecimal refundAmount) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.id(),
                booking.propertyId(),
                booking.roomTypeId(),
                GuestDto.from(booking.guest()),
                booking.stay().checkIn(),
                booking.stay().checkOut(),
                booking.stay().nightCount(),
                booking.guestCount(),
                booking.roomCount(),
                booking.totalPrice().amount(),
                booking.status(),
                booking.holdExpiresAt(),
                booking.cancellationPolicyCode(),
                booking.refundAmount().amount());
    }
}
