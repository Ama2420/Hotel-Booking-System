package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.Money;

/** Port to the external payment provider. The only implementation here is a mock. */
public interface PaymentGateway {

    GatewayResult charge(ChargeRequest request);

    GatewayResult refund(String chargeReference, Money amount);
}
