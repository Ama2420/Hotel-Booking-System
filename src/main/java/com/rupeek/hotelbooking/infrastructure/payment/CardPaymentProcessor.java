package com.rupeek.hotelbooking.infrastructure.payment;

import com.rupeek.hotelbooking.domain.payment.CardInstrument;
import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import com.rupeek.hotelbooking.domain.payment.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor extends GatewayPaymentProcessor<CardInstrument> {

    public CardPaymentProcessor(PaymentGateway gateway) {
        super(gateway, CardInstrument.class);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    protected String tokenize(CardInstrument card) {
        return "card_" + card.last4();
    }
}
