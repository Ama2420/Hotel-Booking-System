package com.rupeek.hotelbooking.domain.search;

import com.rupeek.hotelbooking.domain.property.Property;

/**
 * An optional search refinement. Filters are evaluated per (property, room offer) pair, so a filter can either
 * drop a whole property (e.g. star rating) or narrow it to the room types that qualify (e.g. price).
 *
 * <p>To add a filter: implement this interface, register it as a bean, and add its criterion to
 * {@link SearchQuery}. The search service itself does not change. A filter whose criterion is absent from the
 * query must return {@code true}.
 */
public interface SearchFilter {

    boolean matches(SearchQuery query, Property property, RoomOffer offer);
}
