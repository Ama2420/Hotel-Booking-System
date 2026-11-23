package com.rupeek.hotelbooking.support;

import com.rupeek.hotelbooking.application.BookingService;
import com.rupeek.hotelbooking.application.CancellationService;
import com.rupeek.hotelbooking.application.InventoryService;
import com.rupeek.hotelbooking.application.LockManager;
import com.rupeek.hotelbooking.application.NewProperty;
import com.rupeek.hotelbooking.application.PaymentService;
import com.rupeek.hotelbooking.application.PropertyOnboardingService;
import com.rupeek.hotelbooking.application.PropertySearchService;
import com.rupeek.hotelbooking.domain.booking.BookingPolicy;
import com.rupeek.hotelbooking.domain.cancellation.CancellationPolicyRegistry;
import com.rupeek.hotelbooking.domain.cancellation.FlexibleCancellationPolicy;
import com.rupeek.hotelbooking.domain.cancellation.NonRefundableCancellationPolicy;
import com.rupeek.hotelbooking.domain.payment.PaymentProcessorRegistry;
import com.rupeek.hotelbooking.domain.pricing.StandardPricingStrategy;
import com.rupeek.hotelbooking.domain.property.Owner;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.search.AmenitiesFilter;
import com.rupeek.hotelbooking.domain.search.PriceRangeFilter;
import com.rupeek.hotelbooking.domain.search.StarRatingFilter;
import com.rupeek.hotelbooking.infrastructure.concurrency.InMemoryLockManager;
import com.rupeek.hotelbooking.infrastructure.payment.CardPaymentProcessor;
import com.rupeek.hotelbooking.infrastructure.payment.MockPaymentGateway;
import com.rupeek.hotelbooking.infrastructure.payment.UpiPaymentProcessor;
import com.rupeek.hotelbooking.infrastructure.payment.WalletPaymentProcessor;
import com.rupeek.hotelbooking.infrastructure.persistence.InMemoryBookingRepository;
import com.rupeek.hotelbooking.infrastructure.persistence.InMemoryInventoryRepository;
import com.rupeek.hotelbooking.infrastructure.persistence.InMemoryOwnerRepository;
import com.rupeek.hotelbooking.infrastructure.persistence.InMemoryPaymentRepository;
import com.rupeek.hotelbooking.infrastructure.persistence.InMemoryPropertyRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Wires the real services to the in-memory adapters without Spring, so business-rule tests run in milliseconds
 * and exercise real collaborators rather than mocks.
 */
public class HotelTestContext {

    public static final Instant START = Instant.parse("2026-12-01T06:30:00Z");
    public static final Duration HOLD = Duration.ofMinutes(15);

    public final MutableClock clock = new MutableClock(START, ZoneOffset.UTC);
    public final CountingPaymentGateway gateway = new CountingPaymentGateway(new MockPaymentGateway());
    public final InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
    public final InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();

    public final InventoryService inventoryService;
    public final PropertyOnboardingService onboardingService;
    public final PropertySearchService searchService;
    public final BookingService bookingService;
    public final PaymentService paymentService;
    public final CancellationService cancellationService;

    public HotelTestContext() {
        LockManager locks = new InMemoryLockManager();
        InMemoryPropertyRepository propertyRepository = new InMemoryPropertyRepository();
        BookingPolicy bookingPolicy = new BookingPolicy(30, HOLD);
        StandardPricingStrategy pricing = new StandardPricingStrategy();
        CancellationPolicyRegistry cancellationPolicies = new CancellationPolicyRegistry(List.of(
                new FlexibleCancellationPolicy(Duration.ofHours(24)), new NonRefundableCancellationPolicy()));
        PaymentProcessorRegistry processors = new PaymentProcessorRegistry(List.of(
                new CardPaymentProcessor(gateway), new UpiPaymentProcessor(gateway), new WalletPaymentProcessor(gateway)));

        inventoryService = new InventoryService(new InMemoryInventoryRepository(), locks);
        onboardingService = new PropertyOnboardingService(new InMemoryOwnerRepository(), propertyRepository,
                inventoryService, cancellationPolicies);
        searchService = new PropertySearchService(propertyRepository, inventoryService, pricing, bookingPolicy,
                List.of(new PriceRangeFilter(), new AmenitiesFilter(), new StarRatingFilter()), clock);
        bookingService = new BookingService(propertyRepository, bookingRepository, inventoryService, pricing,
                bookingPolicy, locks, clock);
        paymentService = new PaymentService(bookingService, bookingRepository, paymentRepository, processors,
                gateway, locks, clock);
        cancellationService = new CancellationService(bookingService, bookingRepository, paymentService,
                inventoryService, cancellationPolicies, locks, clock);
    }

    public Owner owner() {
        return onboardingService.registerOwner("Test Owner", "owner@example.com");
    }

    public Property onboard(NewProperty property) {
        return onboardingService.addProperty(owner().id(), property);
    }
}
