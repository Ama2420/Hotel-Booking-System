package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.Money;

/**
 * Takes a payment with one particular {@link PaymentMethod}. Adding a method means adding an instrument type and
 * a processor; nothing that selects or uses processors changes.
 */
public interface PaymentProcessor {

    PaymentMethod method();

    GatewayResult process(PaymentInstrument instrument, Money amount, String merchantReference);
}
