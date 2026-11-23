package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Location;
import com.rupeek.hotelbooking.domain.property.StarRating;
import java.util.List;
import java.util.Set;

/** Onboarding command. {@code cancellationPolicyCode} is optional and defaults to FLEXIBLE. */
public record NewProperty(
        String name,
        Location location,
        StarRating starRating,
        Set<Amenity> amenities,
        List<NewRoomType> roomTypes,
        String cancellationPolicyCode) {

    public NewProperty {
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        roomTypes = List.copyOf(requireNonNull(roomTypes, "roomTypes"));
    }
}
