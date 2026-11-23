package com.rupeek.hotelbooking.support;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.ChargeRequest;
import com.rupeek.hotelbooking.domain.payment.GatewayResult;
import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import java.util.concurrent.atomic.AtomicInteger;

/** Delegating gateway that counts calls, so tests can assert a card was charged exactly once. */
public class CountingPaymentGateway implements PaymentGateway {

    private final PaymentGateway delegate;
    private final AtomicInteger charges = new AtomicInteger();
    private final AtomicInteger refunds = new AtomicInteger();

    public CountingPaymentGateway(PaymentGateway delegate) {
        this.delegate = delegate;
    }

    @Override
    public GatewayResult charge(ChargeRequest request) {
        charges.incrementAndGet();
        return delegate.charge(request);
    }

    @Override
    public GatewayResult refund(String chargeReference, Money amount) {
        refunds.incrementAndGet();
        return delegate.refund(chargeReference, amount);
    }

    public int charges() {
        return charges.get();
    }

    public int refunds() {
        return refunds.get();
    }
}
