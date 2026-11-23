package com.rupeek.hotelbooking.api.booking;

import com.rupeek.hotelbooking.domain.booking.BookingStatus;
import com.rupeek.hotelbooking.domain.payment.Payment;
import com.rupeek.hotelbooking.domain.payment.PaymentMethod;
import com.rupeek.hotelbooking.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        String bookingId,
        PaymentMethod method,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        String gatewayReference,
        BigDecimal refundedAmount,
        Instant createdAt,
        BookingStatus bookingStatus) {

    public static PaymentResponse from(Payment payment, BookingStatus bookingStatus) {
        return new PaymentResponse(
                payment.id(),
                payment.bookingId(),
                payment.method(),
                payment.amount().amount(),
                payment.status(),
                payment.failureReason(),
                payment.gatewayReference(),
                payment.refundedAmount().amount(),
                payment.createdAt(),
                bookingStatus);
    }
}
