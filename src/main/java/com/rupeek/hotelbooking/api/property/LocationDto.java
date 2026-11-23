package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.domain.property.Location;
import jakarta.validation.constraints.NotBlank;

public record LocationDto(@NotBlank String city, String locality) {

    public static LocationDto from(Location location) {
        return new LocationDto(location.city(), location.locality());
    }

    public Location toDomain() {
        return new Location(city, locality);
    }
}
