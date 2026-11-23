package com.rupeek.hotelbooking.infrastructure.payment;

import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import com.rupeek.hotelbooking.domain.payment.PaymentMethod;
import com.rupeek.hotelbooking.domain.payment.UpiInstrument;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor extends GatewayPaymentProcessor<UpiInstrument> {

    public UpiPaymentProcessor(PaymentGateway gateway) {
        super(gateway, UpiInstrument.class);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    protected String tokenize(UpiInstrument upi) {
        return "upi_" + upi.vpa();
    }
}
