package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingPolicy;
import com.rupeek.hotelbooking.domain.booking.BookingRepository;
import com.rupeek.hotelbooking.domain.booking.BookingRequest;
import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.pricing.PricingStrategy;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.PropertyRepository;
import com.rupeek.hotelbooking.domain.property.RoomType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Creates bookings and manages their payment holds. Creating a booking reserves inventory immediately, so a
 * guest who reaches the payment step cannot be outbid by a concurrent booking for the last room.
 */
@Service
public class BookingService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final InventoryService inventoryService;
    private final PricingStrategy pricingStrategy;
    private final BookingPolicy bookingPolicy;
    private final LockManager lockManager;
    private final Clock clock;

    public BookingService(PropertyRepository propertyRepository, BookingRepository bookingRepository,
                          InventoryService inventoryService, PricingStrategy pricingStrategy,
                          BookingPolicy bookingPolicy, LockManager lockManager, Clock clock) {
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.inventoryService = inventoryService;
        this.pricingStrategy = pricingStrategy;
        this.bookingPolicy = bookingPolicy;
        this.lockManager = lockManager;
        this.clock = clock;
    }

    public Booking createBooking(BookingRequest request) {
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.propertyId()));
        RoomType roomType = property.findRoomType(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type", request.roomTypeId()));
        bookingPolicy.validateStay(request.stay(), LocalDate.now(clock));
        ensureCapacity(roomType, request);

        Money totalPrice = pricingStrategy.quote(roomType, request.stay(), request.roomCount());
        inventoryService.reserve(roomType.id(), request.stay(), request.roomCount());

        Instant now = clock.instant();
        Booking booking = Booking.hold(Ids.newId("bk"), request, totalPrice, property.cancellationPolicyCode(),
                now, bookingPolicy.holdExpiryFor(now));
        return bookingRepository.save(booking);
    }

    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    /** Expires every booking whose payment hold has lapsed and returns its rooms to inventory. */
    public int expireLapsedHolds() {
        int expired = 0;
        for (Booking candidate : bookingRepository.findLapsedHolds(clock.instant())) {
            boolean didExpire = lockManager.withLock(LockKeys.booking(candidate.id()),
                    () -> expireIfHoldLapsed(getBooking(candidate.id())));
            expired += didExpire ? 1 : 0;
        }
        return expired;
    }

    /** Caller must hold the booking's lock. Returns whether the booking was expired by this call. */
    boolean expireIfHoldLapsed(Booking booking) {
        if (!booking.isHoldLapsed(clock.instant())) {
            return false;
        }
        booking.expire();
        bookingRepository.save(booking);
        inventoryService.release(booking.roomTypeId(), booking.stay(), booking.roomCount());
        return true;
    }

    private static void ensureCapacity(RoomType roomType, BookingRequest request) {
        if (!roomType.canAccommodate(request.guestCount(), request.roomCount())) {
            throw new BusinessRuleViolationException(String.format(
                    "%d room(s) of '%s' sleep at most %d guests; %d requested",
                    request.roomCount(), roomType.name(), roomType.maxOccupancy() * request.roomCount(),
                    request.guestCount()));
        }
    }
}
