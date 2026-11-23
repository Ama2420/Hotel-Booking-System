package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.property.Property;

/** Keeps properties rated at or above the requested minimum. */
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean matches(SearchQuery query, Property property, RoomOffer offer) {
        return query.minStarRating() == null || property.starRating().isAtLeast(query.minStarRating());
    }
}
