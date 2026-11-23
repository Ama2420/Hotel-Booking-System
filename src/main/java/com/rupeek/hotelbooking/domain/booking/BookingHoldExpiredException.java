package com.rupeek.hotelbooking.domain.booking;

import com.rupeek.hotelbooking.domain.common.DomainException;

public class BookingHoldExpiredException extends DomainException {

    public BookingHoldExpiredException(String bookingId) {
        super("Payment window for booking '" + bookingId + "' has lapsed; the rooms have been released");
    }
}
