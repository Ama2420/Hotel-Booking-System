package com.rupeek.hotelbooking.domain.search;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;
import static com.rupeek.hotelbooking.domain.common.Validation.requirePositive;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.StarRating;
import java.util.Set;

/**
 * Search criteria. City, stay and party size are mandatory; every other field is an optional refinement that is
 * ignored when null/empty. Prices are per room per night.
 */
public record SearchQuery(
        String city,
        String locality,
        DateRange stay,
        int guests,
        int rooms,
        Money minNightlyPrice,
        Money maxNightlyPrice,
        Set<Amenity> amenities,
        Integer minStarRating) {

    public SearchQuery {
        city = requireNonBlank(city, "city");
        requireNonNull(stay, "stay");
        requirePositive(guests, "guests");
        requirePositive(rooms, "rooms");
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        if (minNightlyPrice != null && maxNightlyPrice != null && minNightlyPrice.isGreaterThan(maxNightlyPrice)) {
            throw new IllegalArgumentException("minPrice must not exceed maxPrice");
        }
        if (minStarRating != null) {
            new StarRating(minStarRating);
        }
    }
}
