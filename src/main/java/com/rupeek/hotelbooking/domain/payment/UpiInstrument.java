package com.rupeek.hotelbooking.domain.payment;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

import java.util.regex.Pattern;

public record UpiInstrument(String vpa) implements PaymentInstrument {

    private static final Pattern VPA = Pattern.compile("^[\\w.\\-]{2,}@[a-zA-Z]{2,}$");

    public UpiInstrument {
        vpa = requireNonBlank(vpa, "vpa");
        if (!VPA.matcher(vpa).matches()) {
            throw new IllegalArgumentException("vpa must look like name@bank");
        }
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }
}
