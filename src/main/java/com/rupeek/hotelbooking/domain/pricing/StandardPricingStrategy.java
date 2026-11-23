package com.rupeek.hotelbooking.domain.pricing;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.time.LocalDate;

/** Charges the room type's listed price for every night. */
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public Money nightlyRate(RoomType roomType, LocalDate night) {
        return roomType.pricePerNight();
    }
}
