package com.rupeek.hotelbooking.domain.property;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

import java.util.Locale;

/**
 * A free-form, case-insensitive amenity (e.g. "wifi", "pool"). Kept as a value object rather than an enum so
 * owners can list new amenities without a code change.
 */
public record Amenity(String name) {

    public Amenity {
        name = requireNonBlank(name, "amenity").toLowerCase(Locale.ROOT);
    }

    public static Amenity of(String name) {
        return new Amenity(name);
    }
}
