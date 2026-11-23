package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Property;

/** Keeps room offers whose average nightly rate falls within [minPrice, maxPrice]. */
public class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean matches(SearchQuery query, Property property, RoomOffer offer) {
        Money rate = offer.averageNightlyRate();
        boolean aboveMin = query.minNightlyPrice() == null || rate.compareTo(query.minNightlyPrice()) >= 0;
        boolean belowMax = query.maxNightlyPrice() == null || rate.compareTo(query.maxNightlyPrice()) <= 0;
        return aboveMin && belowMax;
    }
}
