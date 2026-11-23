package com.rupeek.hotelbooking.domain.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Location;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.RoomType;
import com.rupeek.hotelbooking.domain.property.StarRating;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SearchFilterTest {

    private static final DateRange STAY = new DateRange(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
    private static final RoomType ROOM = new RoomType("rt_1", "Deluxe", 2, 5, Money.of("3000"));
    private static final Property PROPERTY = new Property("prop_1", "own_1", "Lakeside",
            new Location("Bengaluru", "Indiranagar"), new StarRating(4),
            Set.of(Amenity.of("wifi"), Amenity.of("pool")), List.of(ROOM), "FLEXIBLE");
    private static final RoomOffer OFFER = new RoomOffer(ROOM, 5, Money.of("6000"), Money.of("3000"));

    private static SearchQuery query(String min, String max, Set<String> amenities, Integer minStars) {
        return new SearchQuery("Bengaluru", null, STAY, 2, 1,
                min == null ? null : Money.of(min), max == null ? null : Money.of(max),
                amenities.stream().map(Amenity::of).collect(Collectors.toSet()), minStars);
    }

    @Test
    void filtersPassWhenTheirCriterionIsAbsent() {
        SearchQuery unrefined = query(null, null, Set.of(), null);

        assertThat(new PriceRangeFilter().matches(unrefined, PROPERTY, OFFER)).isTrue();
        assertThat(new AmenitiesFilter().matches(unrefined, PROPERTY, OFFER)).isTrue();
        assertThat(new StarRatingFilter().matches(unrefined, PROPERTY, OFFER)).isTrue();
    }

    @Test
    void priceRangeIsInclusiveOnBothBounds() {
        PriceRangeFilter filter = new PriceRangeFilter();

        assertThat(filter.matches(query("3000", "3000", Set.of(), null), PROPERTY, OFFER)).isTrue();
        assertThat(filter.matches(query("3001", null, Set.of(), null), PROPERTY, OFFER)).isFalse();
        assertThat(filter.matches(query(null, "2999", Set.of(), null), PROPERTY, OFFER)).isFalse();
    }

    @Test
    void amenitiesMustAllBeOfferedCaseInsensitively() {
        AmenitiesFilter filter = new AmenitiesFilter();

        assertThat(filter.matches(query(null, null, Set.of("WiFi", "pool"), null), PROPERTY, OFFER)).isTrue();
        assertThat(filter.matches(query(null, null, Set.of("wifi", "spa"), null), PROPERTY, OFFER)).isFalse();
    }

    @Test
    void starRatingIsAMinimum() {
        StarRatingFilter filter = new StarRatingFilter();

        assertThat(filter.matches(query(null, null, Set.of(), 4), PROPERTY, OFFER)).isTrue();
        assertThat(filter.matches(query(null, null, Set.of(), 5), PROPERTY, OFFER)).isFalse();
    }
}
