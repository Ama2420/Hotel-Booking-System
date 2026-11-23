package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.booking.BookingPolicy;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.pricing.PricingStrategy;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.PropertyRepository;
import com.rupeek.hotelbooking.domain.property.RoomType;
import com.rupeek.hotelbooking.domain.search.PropertySearchResult;
import com.rupeek.hotelbooking.domain.search.RoomOffer;
import com.rupeek.hotelbooking.domain.search.SearchFilter;
import com.rupeek.hotelbooking.domain.search.SearchQuery;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Finds properties with bookable rooms for a stay. Location, availability and party size are always enforced;
 * everything else is delegated to the registered {@link SearchFilter}s.
 */
@Service
public class PropertySearchService {

    private static final Comparator<PropertySearchResult> CHEAPEST_FIRST = Comparator.comparing(
            result -> result.offers().get(0).averageNightlyRate());

    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;
    private final PricingStrategy pricingStrategy;
    private final BookingPolicy bookingPolicy;
    private final List<SearchFilter> filters;
    private final Clock clock;

    public PropertySearchService(PropertyRepository propertyRepository, InventoryService inventoryService,
                                 PricingStrategy pricingStrategy, BookingPolicy bookingPolicy,
                                 List<SearchFilter> filters, Clock clock) {
        this.propertyRepository = propertyRepository;
        this.inventoryService = inventoryService;
        this.pricingStrategy = pricingStrategy;
        this.bookingPolicy = bookingPolicy;
        this.filters = List.copyOf(filters);
        this.clock = clock;
    }

    public List<PropertySearchResult> search(SearchQuery query) {
        bookingPolicy.validateStay(query.stay(), LocalDate.now(clock));
        return propertyRepository.findByCity(query.city()).stream()
                .filter(property -> property.location().isInLocality(query.locality()))
                .map(property -> matchingOffers(property, query))
                .flatMap(Optional::stream)
                .sorted(CHEAPEST_FIRST)
                .toList();
    }

    private Optional<PropertySearchResult> matchingOffers(Property property, SearchQuery query) {
        List<RoomOffer> offers = property.roomTypes().stream()
                .filter(roomType -> roomType.canAccommodate(query.guests(), query.rooms()))
                .map(roomType -> availableOffer(roomType, query))
                .flatMap(Optional::stream)
                .filter(offer -> passesAllFilters(query, property, offer))
                .sorted(Comparator.comparing(RoomOffer::averageNightlyRate))
                .toList();
        return offers.isEmpty() ? Optional.empty() : Optional.of(new PropertySearchResult(property, offers));
    }

    private Optional<RoomOffer> availableOffer(RoomType roomType, SearchQuery query) {
        int available = inventoryService.availableUnits(roomType.id(), query.stay());
        if (available < query.rooms()) {
            return Optional.empty();
        }
        Money total = pricingStrategy.quote(roomType, query.stay(), query.rooms());
        long roomNights = query.stay().nightCount() * query.rooms();
        return Optional.of(RoomOffer.of(roomType, available, total, roomNights));
    }

    private boolean passesAllFilters(SearchQuery query, Property property, RoomOffer offer) {
        return filters.stream().allMatch(filter -> filter.matches(query, property, offer));
    }
}
