package com.rupeek.hotelbooking.infrastructure.persistence;

import com.rupeek.hotelbooking.domain.payment.Payment;
import com.rupeek.hotelbooking.domain.payment.PaymentRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentRepository extends InMemoryRepository<Payment> implements PaymentRepository {

    public InMemoryPaymentRepository() {
        super(Payment::id);
    }

    @Override
    public Optional<Payment> findByBookingIdAndIdempotencyKey(String bookingId, String idempotencyKey) {
        return all()
                .filter(payment -> payment.bookingId().equals(bookingId))
                .filter(payment -> payment.idempotencyKey().equals(idempotencyKey))
                .findFirst();
    }

    @Override
    public Optional<Payment> findSuccessfulByBookingId(String bookingId) {
        return all()
                .filter(payment -> payment.bookingId().equals(bookingId))
                .filter(Payment::isSuccessful)
                .findFirst();
    }
}
