package com.rupeek.hotelbooking.domain.booking;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;
import static com.rupeek.hotelbooking.domain.common.Validation.requirePositive;

/** What a guest asks to book. Pricing, policy and hold expiry are decided by the platform, not the caller. */
public record BookingRequest(
        String propertyId,
        String roomTypeId,
        Guest guest,
        DateRange stay,
        int guestCount,
        int roomCount) {

    public BookingRequest {
        propertyId = requireNonBlank(propertyId, "propertyId");
        roomTypeId = requireNonBlank(roomTypeId, "roomTypeId");
        requireNonNull(guest, "guest");
        requireNonNull(stay, "stay");
        requirePositive(guestCount, "guests");
        requirePositive(roomCount, "rooms");
    }
}
