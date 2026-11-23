package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.property.Property;

/** Keeps properties that offer every requested amenity. */
public class AmenitiesFilter implements SearchFilter {

    @Override
    public boolean matches(SearchQuery query, Property property, RoomOffer offer) {
        return property.offersAll(query.amenities());
    }
}
