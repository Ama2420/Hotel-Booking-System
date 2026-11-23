package com.rupeek.hotelbooking.infrastructure.payment;

import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.ChargeRequest;
import com.rupeek.hotelbooking.domain.payment.GatewayResult;
import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import com.rupeek.hotelbooking.domain.payment.PaymentInstrument;
import com.rupeek.hotelbooking.domain.payment.PaymentProcessor;

/**
 * Shared flow for methods that settle through the payment gateway: check the instrument type, turn it into an
 * opaque token, and charge. Subclasses only say which instrument they accept and how to tokenise it.
 */
public abstract class GatewayPaymentProcessor<I extends PaymentInstrument> implements PaymentProcessor {

    private final PaymentGateway gateway;
    private final Class<I> instrumentType;

    protected GatewayPaymentProcessor(PaymentGateway gateway, Class<I> instrumentType) {
        this.gateway = gateway;
        this.instrumentType = instrumentType;
    }

    @Override
    public GatewayResult process(PaymentInstrument instrument, Money amount, String merchantReference) {
        if (!instrumentType.isInstance(instrument)) {
            throw new BusinessRuleViolationException(
                    method() + " payments require a " + instrumentType.getSimpleName());
        }
        String token = tokenize(instrumentType.cast(instrument));
        return gateway.charge(new ChargeRequest(merchantReference, amount, method(), token));
    }

    protected abstract String tokenize(I instrument);
}
