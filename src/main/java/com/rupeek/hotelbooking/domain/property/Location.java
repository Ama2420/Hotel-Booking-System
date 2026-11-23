package com.rupeek.hotelbooking.domain.property;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

public record Location(String city, String locality) {

    public Location {
        city = requireNonBlank(city, "city");
        locality = locality == null || locality.isBlank() ? null : locality.trim();
    }

    public boolean isInCity(String otherCity) {
        return otherCity != null && city.equalsIgnoreCase(otherCity.trim());
    }

    /** A blank locality is a wildcard and matches every locality in the city. */
    public boolean isInLocality(String otherLocality) {
        return otherLocality == null || otherLocality.isBlank() || otherLocality.trim().equalsIgnoreCase(locality);
    }
}
