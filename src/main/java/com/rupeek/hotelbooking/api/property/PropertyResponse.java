package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Property;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public record PropertyResponse(
        String id,
        String ownerId,
        String name,
        LocationDto location,
        int starRating,
        Set<String> amenities,
        List<RoomTypeResponse> roomTypes,
        String cancellationPolicy) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.id(),
                property.ownerId(),
                property.name(),
                LocationDto.from(property.location()),
                property.starRating().value(),
                amenityNames(property.amenities()),
                property.roomTypes().stream().map(RoomTypeResponse::from).toList(),
                property.cancellationPolicyCode());
    }

    static Set<String> amenityNames(Set<Amenity> amenities) {
        return amenities.stream().map(Amenity::name).collect(Collectors.toCollection(TreeSet::new));
    }
}
