package com.rupeek.hotelbooking.domain.property;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;
import static com.rupeek.hotelbooking.domain.common.Validation.requirePositive;

import com.rupeek.hotelbooking.domain.common.Money;

/**
 * A bookable category of rooms within a property (e.g. "Deluxe King"). {@code totalUnits} is the number of
 * identical physical rooms that make up its inventory.
 */
public record RoomType(String id, String name, int maxOccupancy, int totalUnits, Money pricePerNight) {

    public RoomType {
        id = requireNonBlank(id, "roomType.id");
        name = requireNonBlank(name, "roomType.name");
        requirePositive(maxOccupancy, "maxOccupancy");
        requirePositive(totalUnits, "totalUnits");
        requireNonNull(pricePerNight, "pricePerNight");
        if (pricePerNight.isZero()) {
            throw new IllegalArgumentException("pricePerNight must be greater than zero");
        }
    }

    public boolean canAccommodate(int guests, int rooms) {
        return (long) maxOccupancy * rooms >= guests;
    }
}
