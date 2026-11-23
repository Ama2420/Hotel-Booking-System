package com.rupeek.hotelbooking.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hotel")
public record HotelProperties(BookingSettings booking, PricingSettings pricing) {

    public record BookingSettings(Duration holdDuration, int maxNights, Duration freeCancellationWindow) {
    }

    public record PricingSettings(PricingStrategyType strategy, int weekendSurchargePercent) {
    }

    public enum PricingStrategyType {
        STANDARD,
        WEEKEND_SURCHARGE
    }
}
