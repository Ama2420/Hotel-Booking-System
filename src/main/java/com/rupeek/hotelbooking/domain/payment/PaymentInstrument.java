package com.rupeek.hotelbooking.domain.payment;

/** The guest-supplied means of paying, e.g. a card or a UPI handle. Each method has its own instrument type. */
public interface PaymentInstrument {

    PaymentMethod method();
}
