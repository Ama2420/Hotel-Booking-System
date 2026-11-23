package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.DomainException;

public class IdempotencyKeyConflictException extends DomainException {

    public IdempotencyKeyConflictException(String idempotencyKey) {
        super("Idempotency-Key '" + idempotencyKey + "' was already used for a different payment request");
    }
}
