package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.DomainException;

public class PaymentGatewayException extends DomainException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}
