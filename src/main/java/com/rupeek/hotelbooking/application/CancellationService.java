package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingRepository;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.cancellation.CancellationPolicyRegistry;
import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Cancels a booking: applies the cancellation policy the guest booked under, refunds through the gateway, and
 * returns the rooms to inventory so they are discoverable again.
 */
@Service
public class CancellationService {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final CancellationPolicyRegistry cancellationPolicies;
    private final LockManager lockManager;
    private final Clock clock;

    public CancellationService(BookingService bookingService, BookingRepository bookingRepository,
                               PaymentService paymentService, InventoryService inventoryService,
                               CancellationPolicyRegistry cancellationPolicies, LockManager lockManager,
                               Clock clock) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.cancellationPolicies = cancellationPolicies;
        this.lockManager = lockManager;
        this.clock = clock;
    }

    public Booking cancel(String bookingId) {
        return lockManager.withLock(LockKeys.booking(bookingId), () -> {
            Booking booking = bookingService.getBooking(bookingId);
            LocalDateTime now = LocalDateTime.now(clock);
            ensureStayNotStarted(booking, now);
            booking.assertCanTransitionTo(BookingStatus.CANCELLED);

            Money refund = refundFor(booking, now);
            paymentService.refund(bookingId, refund);
            booking.cancel(refund);
            bookingRepository.save(booking);
            inventoryService.release(booking.roomTypeId(), booking.stay(), booking.roomCount());
            return booking;
        });
    }

    private Money refundFor(Booking booking, LocalDateTime now) {
        Money amountPaid = paymentService.amountPaid(booking.id());
        return cancellationPolicies.forCode(booking.cancellationPolicyCode()).refundFor(booking, amountPaid, now);
    }

    private static void ensureStayNotStarted(Booking booking, LocalDateTime now) {
        if (booking.hasStayStarted(now)) {
            throw new BusinessRuleViolationException(
                    "Booking '" + booking.id() + "' cannot be cancelled after check-in");
        }
    }
}
