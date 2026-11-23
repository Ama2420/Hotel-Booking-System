package com.rupeek.hotelbooking.support;

import com.rupeek.hotelbooking.application.NewProperty;
import com.rupeek.hotelbooking.application.NewRoomType;
import com.rupeek.hotelbooking.domain.booking.BookingRequest;
import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.booking.Guest;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Location;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.StarRating;
import com.rupeek.hotelbooking.domain.search.PropertySearchResult;
import com.rupeek.hotelbooking.domain.search.SearchQuery;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TestData {

    /** Test "today" is {@link HotelTestContext#START}: Tuesday 2026-12-01. */
    public static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 10);
    public static final LocalDate CHECK_OUT = LocalDate.of(2026, 12, 12);
    public static final DateRange STAY = new DateRange(CHECK_IN, CHECK_OUT);
    public static final Guest GUEST = new Guest("Asha Rao", "asha@example.com");

    private TestData() {
    }

    public static NewRoomType roomType(String name, int maxOccupancy, int units, String pricePerNight) {
        return new NewRoomType(name, maxOccupancy, units, Money.of(pricePerNight));
    }

    public static NewProperty property(String name, String city, int stars, String policy, Set<String> amenities,
                                       NewRoomType... roomTypes) {
        return new NewProperty(name, new Location(city, "Central"), new StarRating(stars),
                amenities.stream().map(Amenity::of).collect(Collectors.toSet()),
                Arrays.asList(roomTypes), policy);
    }

    public static NewProperty hotelWith(String policy, NewRoomType... roomTypes) {
        return property("Test Hotel", "Bengaluru", 4, policy, Set.of("wifi"), roomTypes);
    }

    public static BookingRequest bookingFor(Property property, DateRange stay, int guests, int rooms) {
        return new BookingRequest(property.id(), property.roomTypes().get(0).id(), GUEST, stay, guests, rooms);
    }

    public static BookingRequest bookingFor(Property property) {
        return bookingFor(property, STAY, 2, 1);
    }

    public static SearchQuery search(String city, DateRange stay, int guests) {
        return new SearchQuery(city, null, stay, guests, 1, null, null, Set.of(), null);
    }

    public static List<String> names(List<PropertySearchResult> results) {
        return results.stream().map(result -> result.property().name()).toList();
    }
}
