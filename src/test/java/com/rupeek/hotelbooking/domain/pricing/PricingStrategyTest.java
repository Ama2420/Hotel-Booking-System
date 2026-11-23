package com.rupeek.hotelbooking.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricingStrategyTest {

    private static final RoomType DELUXE = new RoomType("rt_1", "Deluxe", 2, 5, Money.of("1000"));
    /** Thursday 10 Dec to Sunday 13 Dec 2026: nights of Thu, Fri, Sat. */
    private static final DateRange THU_TO_SUN = new DateRange(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13));

    @Test
    void standardPricingChargesListPricePerNightPerRoom() {
        Money quote = new StandardPricingStrategy().quote(DELUXE, THU_TO_SUN, 2);

        assertThat(quote).isEqualTo(Money.of("6000"));
    }

    @Test
    void weekendSurchargeAppliesToFridayAndSaturdayNightsOnly() {
        Money quote = new WeekendSurchargePricingStrategy(20).quote(DELUXE, THU_TO_SUN, 1);

        assertThat(quote).isEqualTo(Money.of("3400"));
    }
}
