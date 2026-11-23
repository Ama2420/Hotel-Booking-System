package com.rupeek.hotelbooking.config;

import com.rupeek.hotelbooking.domain.booking.BookingPolicy;
import com.rupeek.hotelbooking.domain.cancellation.CancellationPolicy;
import com.rupeek.hotelbooking.domain.cancellation.CancellationPolicyRegistry;
import com.rupeek.hotelbooking.domain.cancellation.FlexibleCancellationPolicy;
import com.rupeek.hotelbooking.domain.cancellation.NonRefundableCancellationPolicy;
import com.rupeek.hotelbooking.domain.payment.PaymentProcessor;
import com.rupeek.hotelbooking.domain.payment.PaymentProcessorRegistry;
import com.rupeek.hotelbooking.domain.pricing.PricingStrategy;
import com.rupeek.hotelbooking.domain.pricing.StandardPricingStrategy;
import com.rupeek.hotelbooking.domain.pricing.WeekendSurchargePricingStrategy;
import com.rupeek.hotelbooking.domain.search.AmenitiesFilter;
import com.rupeek.hotelbooking.domain.search.PriceRangeFilter;
import com.rupeek.hotelbooking.domain.search.SearchFilter;
import com.rupeek.hotelbooking.domain.search.StarRatingFilter;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wires the framework-free domain policies into Spring. Each extension point (search filters, cancellation
 * policies, payment processors) is collected as a list of beans, so adding one is a new bean, not an edit to
 * the code that uses them.
 */
@Configuration
@EnableScheduling
public class HotelBookingConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public BookingPolicy bookingPolicy(HotelProperties properties) {
        HotelProperties.BookingSettings booking = properties.booking();
        return new BookingPolicy(booking.maxNights(), booking.holdDuration());
    }

    @Bean
    public PricingStrategy pricingStrategy(HotelProperties properties) {
        HotelProperties.PricingSettings pricing = properties.pricing();
        return switch (pricing.strategy()) {
            case STANDARD -> new StandardPricingStrategy();
            case WEEKEND_SURCHARGE -> new WeekendSurchargePricingStrategy(pricing.weekendSurchargePercent());
        };
    }

    @Bean
    public SearchFilter priceRangeFilter() {
        return new PriceRangeFilter();
    }

    @Bean
    public SearchFilter amenitiesFilter() {
        return new AmenitiesFilter();
    }

    @Bean
    public SearchFilter starRatingFilter() {
        return new StarRatingFilter();
    }

    @Bean
    public CancellationPolicy flexibleCancellationPolicy(HotelProperties properties) {
        return new FlexibleCancellationPolicy(properties.booking().freeCancellationWindow());
    }

    @Bean
    public CancellationPolicy nonRefundableCancellationPolicy() {
        return new NonRefundableCancellationPolicy();
    }

    @Bean
    public CancellationPolicyRegistry cancellationPolicyRegistry(List<CancellationPolicy> policies) {
        return new CancellationPolicyRegistry(policies);
    }

    @Bean
    public PaymentProcessorRegistry paymentProcessorRegistry(List<PaymentProcessor> processors) {
        return new PaymentProcessorRegistry(processors);
    }
}
