package com.rupeek.hotelbooking.domain.property;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public record Property(
        String id,
        String ownerId,
        String name,
        Location location,
        StarRating starRating,
        Set<Amenity> amenities,
        List<RoomType> roomTypes,
        String cancellationPolicyCode) {

    public Property {
        id = requireNonBlank(id, "property.id");
        ownerId = requireNonBlank(ownerId, "ownerId");
        name = requireNonBlank(name, "property.name");
        requireNonNull(location, "location");
        requireNonNull(starRating, "starRating");
        amenities = Set.copyOf(requireNonNull(amenities, "amenities"));
        roomTypes = List.copyOf(requireNonNull(roomTypes, "roomTypes"));
        cancellationPolicyCode = requireNonBlank(cancellationPolicyCode, "cancellationPolicy");
        if (roomTypes.isEmpty()) {
            throw new IllegalArgumentException("a property must have at least one room type");
        }
        requireUniqueRoomTypeNames(roomTypes);
    }

    public Optional<RoomType> findRoomType(String roomTypeId) {
        return roomTypes.stream().filter(roomType -> roomType.id().equals(roomTypeId)).findFirst();
    }

    public boolean offersAll(Set<Amenity> required) {
        return amenities.containsAll(required);
    }

    private static void requireUniqueRoomTypeNames(List<RoomType> roomTypes) {
        Set<String> seen = new HashSet<>();
        roomTypes.stream()
                .map(roomType -> roomType.name().toLowerCase(Locale.ROOT))
                .filter(name -> !seen.add(name))
                .findFirst()
                .ifPresent(duplicate -> {
                    throw new IllegalArgumentException("duplicate room type name: " + duplicate);
                });
    }
}
