package com.rupeek.hotelbooking.domain.pricing;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.time.LocalDate;

/**
 * Decides what a room costs. Strategies normally only vary the nightly rate; a strategy that needs a
 * whole-stay view (e.g. length-of-stay discounts) can override {@link #quote}.
 */
public interface PricingStrategy {

    Money nightlyRate(RoomType roomType, LocalDate night);

    default Money quote(RoomType roomType, DateRange stay, int roomCount) {
        return stay.nights()
                .map(night -> nightlyRate(roomType, night))
                .reduce(Money.ZERO, Money::plus)
                .times(roomCount);
    }
}
