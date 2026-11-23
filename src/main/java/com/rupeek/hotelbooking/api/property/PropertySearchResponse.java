package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.search.PropertySearchResult;
import com.rupeek.hotelbooking.domain.search.RoomOffer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PropertySearchResponse(
        String propertyId,
        String name,
        LocationDto location,
        int starRating,
        Set<String> amenities,
        List<RoomOfferResponse> availableRooms) {

    public record RoomOfferResponse(
            String roomTypeId,
            String name,
            int maxOccupancy,
            int availableUnits,
            BigDecimal averageNightlyRate,
            BigDecimal totalPrice) {

        static RoomOfferResponse from(RoomOffer offer) {
            return new RoomOfferResponse(
                    offer.roomType().id(),
                    offer.roomType().name(),
                    offer.roomType().maxOccupancy(),
                    offer.availableUnits(),
                    offer.averageNightlyRate().amount(),
                    offer.totalPrice().amount());
        }
    }

    public static PropertySearchResponse from(PropertySearchResult result) {
        Property property = result.property();
        return new PropertySearchResponse(
                property.id(),
                property.name(),
                LocationDto.from(property.location()),
                property.starRating().value(),
                PropertyResponse.amenityNames(property.amenities()),
                result.offers().stream().map(RoomOfferResponse::from).toList());
    }
}
