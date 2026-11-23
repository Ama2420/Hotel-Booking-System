package com.rupeek.hotelbooking.api.booking;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rupeek.hotelbooking.domain.payment.CardInstrument;
import com.rupeek.hotelbooking.domain.payment.PaymentInstrument;
import com.rupeek.hotelbooking.domain.payment.UpiInstrument;
import com.rupeek.hotelbooking.domain.payment.WalletInstrument;
import jakarta.validation.constraints.NotBlank;

/**
 * Payment request body, discriminated by {@code method}, e.g. {@code {"method":"UPI","vpa":"guest@okaxis"}}.
 * A new payment method adds one subtype here.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaymentInstrumentRequest.Card.class, name = "CARD"),
        @JsonSubTypes.Type(value = PaymentInstrumentRequest.Upi.class, name = "UPI"),
        @JsonSubTypes.Type(value = PaymentInstrumentRequest.Wallet.class, name = "WALLET")
})
public sealed interface PaymentInstrumentRequest {

    PaymentInstrument toDomain();

    record Card(@NotBlank String cardNumber, @NotBlank String holderName) implements PaymentInstrumentRequest {

        @Override
        public PaymentInstrument toDomain() {
            return new CardInstrument(cardNumber, holderName);
        }
    }

    record Upi(@NotBlank String vpa) implements PaymentInstrumentRequest {

        @Override
        public PaymentInstrument toDomain() {
            return new UpiInstrument(vpa);
        }
    }

    record Wallet(@NotBlank String provider, @NotBlank String walletId) implements PaymentInstrumentRequest {

        @Override
        public PaymentInstrument toDomain() {
            return new WalletInstrument(provider, walletId);
        }
    }
}
