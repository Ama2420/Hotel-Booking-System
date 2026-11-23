package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.RoomType;

/** A room type that can satisfy a search, priced for the requested stay and number of rooms. */
public record RoomOffer(RoomType roomType, int availableUnits, Money totalPrice, Money averageNightlyRate) {

    public static RoomOffer of(RoomType roomType, int availableUnits, Money totalPrice, long roomNights) {
        return new RoomOffer(roomType, availableUnits, totalPrice, totalPrice.dividedBy(roomNights));
    }
}
