package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.domain.property.RoomType;
import java.math.BigDecimal;

public record RoomTypeResponse(String id, String name, int maxOccupancy, int totalUnits, BigDecimal pricePerNight) {

    public static RoomTypeResponse from(RoomType roomType) {
        return new RoomTypeResponse(roomType.id(), roomType.name(), roomType.maxOccupancy(), roomType.totalUnits(),
                roomType.pricePerNight().amount());
    }
}
