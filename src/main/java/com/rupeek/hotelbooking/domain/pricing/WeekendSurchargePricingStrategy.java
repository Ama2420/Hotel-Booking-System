package com.rupeek.hotelbooking.domain.pricing;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/** Adds a percentage surcharge to Friday and Saturday nights. */
public class WeekendSurchargePricingStrategy implements PricingStrategy {

    private static final Set<DayOfWeek> WEEKEND_NIGHTS = Set.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private final int surchargePercent;

    public WeekendSurchargePricingStrategy(int surchargePercent) {
        if (surchargePercent < 0) {
            throw new IllegalArgumentException("surchargePercent must not be negative");
        }
        this.surchargePercent = surchargePercent;
    }

    @Override
    public Money nightlyRate(RoomType roomType, LocalDate night) {
        Money base = roomType.pricePerNight();
        return WEEKEND_NIGHTS.contains(night.getDayOfWeek()) ? base.plus(base.percent(surchargePercent)) : base;
    }
}
