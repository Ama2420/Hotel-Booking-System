package com.rupeek.hotelbooking.domain.payment;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

import java.util.regex.Pattern;

public record CardInstrument(String cardNumber, String holderName) implements PaymentInstrument {

    private static final Pattern CARD_NUMBER = Pattern.compile("\\d{12,19}");

    public CardInstrument {
        cardNumber = requireNonBlank(cardNumber, "cardNumber").replace(" ", "");
        if (!CARD_NUMBER.matcher(cardNumber).matches()) {
            throw new IllegalArgumentException("cardNumber must contain 12 to 19 digits");
        }
        holderName = requireNonBlank(holderName, "holderName");
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    public String last4() {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /** Never render the full card number, e.g. in logs. */
    @Override
    public String toString() {
        return "CardInstrument[**** " + last4() + ", " + holderName + "]";
    }
}
