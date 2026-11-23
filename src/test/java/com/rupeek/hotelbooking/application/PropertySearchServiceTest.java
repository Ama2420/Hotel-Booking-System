package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.bookingFor;
import static com.rupeek.hotelbooking.support.TestData.names;
import static com.rupeek.hotelbooking.support.TestData.property;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static com.rupeek.hotelbooking.support.TestData.search;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.search.PropertySearchResult;
import com.rupeek.hotelbooking.domain.search.SearchQuery;
import com.rupeek.hotelbooking.support.HotelTestContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertySearchServiceTest {

    private final HotelTestContext ctx = new HotelTestContext();
    private Property budget;
    private Property luxury;

    @BeforeEach
    void onboardProperties() {
        budget = ctx.onboard(property("Budget Inn", "Bengaluru", 3, "FLEXIBLE", Set.of("wifi"),
                roomType("Standard", 2, 1, "2000")));
        luxury = ctx.onboard(property("Grand Palace", "Bengaluru", 5, "FLEXIBLE", Set.of("wifi", "pool"),
                roomType("Deluxe", 2, 3, "8000"), roomType("Family Suite", 4, 1, "12000")));
        ctx.onboard(property("Beach Shack", "Goa", 3, "FLEXIBLE", Set.of("wifi"), roomType("Hut", 2, 5, "1500")));
    }

    @Test
    void findsPropertiesInTheCityCheapestFirst() {
        List<PropertySearchResult> results = ctx.searchService.search(search("bengaluru", STAY, 2));

        assertThat(names(results)).containsExactly("Budget Inn", "Grand Palace");
    }

    @Test
    void pricesOffersForTheWholeStay() {
        PropertySearchResult result = ctx.searchService.search(search("Bengaluru", STAY, 2)).get(0);

        assertThat(result.offers().get(0).totalPrice()).isEqualTo(Money.of("4000"));
        assertThat(result.offers().get(0).averageNightlyRate()).isEqualTo(Money.of("2000"));
    }

    @Test
    void onlyOffersRoomTypesThatFitTheParty() {
        List<PropertySearchResult> results = ctx.searchService.search(search("Bengaluru", STAY, 4));

        assertThat(names(results)).containsExactly("Grand Palace");
        assertThat(results.get(0).offers()).extracting(offer -> offer.roomType().name())
                .containsExactly("Family Suite");
    }

    @Test
    void hidesPropertiesWithNoAvailabilityForOverlappingDates() {
        ctx.bookingService.createBooking(bookingFor(budget));
        DateRange overlapping = new DateRange(STAY.checkIn().plusDays(1), STAY.checkOut().plusDays(1));
        DateRange afterCheckOut = new DateRange(STAY.checkOut(), STAY.checkOut().plusDays(2));

        assertThat(names(ctx.searchService.search(search("Bengaluru", overlapping, 2))))
                .containsExactly("Grand Palace");
        assertThat(names(ctx.searchService.search(search("Bengaluru", afterCheckOut, 2))))
                .contains("Budget Inn");
    }

    @Test
    void cancelledBookingMakesThePropertyDiscoverableAgain() {
        var booking = ctx.bookingService.createBooking(bookingFor(budget));
        ctx.cancellationService.cancel(booking.id());

        assertThat(names(ctx.searchService.search(search("Bengaluru", STAY, 2)))).contains("Budget Inn");
    }

    @Test
    void appliesFiltersTogether() {
        SearchQuery query = new SearchQuery("Bengaluru", null, STAY, 2, 1, Money.of("5000"), Money.of("10000"),
                Set.of(Amenity.of("pool")), 4);

        List<PropertySearchResult> results = ctx.searchService.search(query);

        assertThat(names(results)).containsExactly("Grand Palace");
        assertThat(results.get(0).offers()).extracting(offer -> offer.roomType().name()).containsExactly("Deluxe");
    }

    @Test
    void narrowsByLocalityWhenGiven() {
        SearchQuery query = new SearchQuery("Bengaluru", "Nowhere", STAY, 2, 1, null, null, Set.of(), null);

        assertThat(ctx.searchService.search(query)).isEmpty();
    }

    @Test
    void rejectsStaysInThePast() {
        DateRange past = new DateRange(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22));

        assertThatThrownBy(() -> ctx.searchService.search(search("Bengaluru", past, 2)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
