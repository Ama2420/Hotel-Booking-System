package com.rupeek.hotelbooking.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(String id);

    Optional<Payment> findByBookingIdAndIdempotencyKey(String bookingId, String idempotencyKey);

    Optional<Payment> findSuccessfulByBookingId(String bookingId);
}
