package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.Money;

/**
 * What the platform sends to the payment gateway. {@code instrumentToken} is an opaque reference to the
 * instrument; raw card numbers never leave the processor.
 */
public record ChargeRequest(String merchantReference, Money amount, PaymentMethod method, String instrumentToken) {
}
