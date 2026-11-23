package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.booking.BookingHoldExpiredException;
import com.rupeek.hotelbooking.domain.booking.BookingRepository;
import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.GatewayResult;
import com.rupeek.hotelbooking.domain.payment.IdempotencyKeyConflictException;
import com.rupeek.hotelbooking.domain.payment.Payment;
import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import com.rupeek.hotelbooking.domain.payment.PaymentGatewayException;
import com.rupeek.hotelbooking.domain.payment.PaymentInstrument;
import com.rupeek.hotelbooking.domain.payment.PaymentProcessorRegistry;
import com.rupeek.hotelbooking.domain.payment.PaymentRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;

/**
 * Takes payment for a booking and lets the outcome drive the booking: success confirms it, a decline leaves it
 * awaiting payment so the guest can retry before the hold lapses.
 *
 * <p>Payments are idempotent per (booking, Idempotency-Key). Everything runs under the booking's lock, so a
 * retried request, a concurrent cancellation and the hold-expiry sweep cannot interleave with a charge.
 */
@Service
public class PaymentService {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorRegistry processors;
    private final PaymentGateway paymentGateway;
    private final LockManager lockManager;
    private final Clock clock;

    public PaymentService(BookingService bookingService, BookingRepository bookingRepository,
                          PaymentRepository paymentRepository, PaymentProcessorRegistry processors,
                          PaymentGateway paymentGateway, LockManager lockManager, Clock clock) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.processors = processors;
        this.paymentGateway = paymentGateway;
        this.lockManager = lockManager;
        this.clock = clock;
    }

    public Payment pay(String bookingId, String idempotencyKey, PaymentInstrument instrument) {
        String key = requireNonBlank(idempotencyKey, "Idempotency-Key");
        requireNonNull(instrument, "payment instrument");
        return lockManager.withLock(LockKeys.booking(bookingId), () ->
                paymentRepository.findByBookingIdAndIdempotencyKey(bookingId, key)
                        .map(previous -> replay(previous, instrument))
                        .orElseGet(() -> charge(bookingService.getBooking(bookingId), key, instrument)));
    }

    public Money amountPaid(String bookingId) {
        return paymentRepository.findSuccessfulByBookingId(bookingId).map(Payment::amount).orElse(Money.ZERO);
    }

    /** Caller must hold the booking's lock. */
    void refund(String bookingId, Money amount) {
        if (amount.isZero()) {
            return;
        }
        Payment payment = paymentRepository.findSuccessfulByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("no successful payment to refund for " + bookingId));
        GatewayResult result = paymentGateway.refund(payment.gatewayReference(), amount);
        if (!result.successful()) {
            throw new PaymentGatewayException("Refund for booking '" + bookingId + "' failed: " + result.failureReason());
        }
        payment.recordRefund(amount);
        paymentRepository.save(payment);
    }

    private Payment replay(Payment previous, PaymentInstrument instrument) {
        if (previous.method() != instrument.method()) {
            throw new IdempotencyKeyConflictException(previous.idempotencyKey());
        }
        return previous;
    }

    private Payment charge(Booking booking, String idempotencyKey, PaymentInstrument instrument) {
        if (bookingService.expireIfHoldLapsed(booking)) {
            throw new BookingHoldExpiredException(booking.id());
        }
        booking.assertCanTransitionTo(BookingStatus.CONFIRMED);

        GatewayResult result = processors.processorFor(instrument.method())
                .process(instrument, booking.totalPrice(), booking.id());
        Payment payment = paymentRepository.save(Payment.record(Ids.newId("pay"), booking, idempotencyKey,
                instrument.method(), result, clock.instant()));

        if (payment.isSuccessful()) {
            booking.confirm();
            bookingRepository.save(booking);
        }
        return payment;
    }
}
