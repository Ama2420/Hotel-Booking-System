package com.rupeek.hotelbooking.domain.payment;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonNull;

import com.rupeek.hotelbooking.domain.booking.Booking;
import com.rupeek.hotelbooking.domain.common.Money;
import java.time.Instant;

/**
 * One payment attempt against a booking, successful or not. The {@code idempotencyKey} is unique per booking:
 * retrying with the same key returns this record instead of charging again.
 */
public class Payment {

    private final String id;
    private final String bookingId;
    private final String idempotencyKey;
    private final PaymentMethod method;
    private final Money amount;
    private final PaymentStatus status;
    private final String gatewayReference;
    private final String failureReason;
    private final Instant createdAt;
    private volatile Money refundedAmount = Money.ZERO;

    private Payment(String id, Booking booking, String idempotencyKey, PaymentMethod method, GatewayResult result,
                    Instant createdAt) {
        this.id = requireNonBlank(id, "payment.id");
        this.bookingId = booking.id();
        this.idempotencyKey = requireNonBlank(idempotencyKey, "idempotencyKey");
        this.method = requireNonNull(method, "method");
        this.amount = booking.totalPrice();
        this.status = result.successful() ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        this.gatewayReference = result.reference();
        this.failureReason = result.failureReason();
        this.createdAt = requireNonNull(createdAt, "createdAt");
    }

    public static Payment record(String id, Booking booking, String idempotencyKey, PaymentMethod method,
                                 GatewayResult result, Instant createdAt) {
        requireNonNull(booking, "booking");
        requireNonNull(result, "gatewayResult");
        return new Payment(id, booking, idempotencyKey, method, result, createdAt);
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCEEDED;
    }

    public Money refundableAmount() {
        return isSuccessful() ? amount.minusFloorZero(refundedAmount) : Money.ZERO;
    }

    public void recordRefund(Money refund) {
        requireNonNull(refund, "refund");
        if (!isSuccessful()) {
            throw new IllegalStateException("only a successful payment can be refunded");
        }
        if (refund.isGreaterThan(refundableAmount())) {
            throw new IllegalArgumentException("refund exceeds the refundable amount of payment " + id);
        }
        this.refundedAmount = refundedAmount.plus(refund);
    }

    public String id() {
        return id;
    }

    public String bookingId() {
        return bookingId;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public PaymentMethod method() {
        return method;
    }

    public Money amount() {
        return amount;
    }

    public PaymentStatus status() {
        return status;
    }

    public String gatewayReference() {
        return gatewayReference;
    }

    public String failureReason() {
        return failureReason;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Money refundedAmount() {
        return refundedAmount;
    }
}
