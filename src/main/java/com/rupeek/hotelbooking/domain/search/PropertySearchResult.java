package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.property.Property;
import java.util.List;

public record PropertySearchResult(Property property, List<RoomOffer> offers) {

    public PropertySearchResult {
        offers = List.copyOf(offers);
    }
}
