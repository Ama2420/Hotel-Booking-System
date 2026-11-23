package com.rupeek.hotelbooking.domain.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A non-negative amount in the platform currency (INR), always held at two decimal places.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Validation.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money must not be negative: " + amount);
        }
        amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    /** Subtracts {@code other}, flooring the result at zero. */
    public Money minusFloorZero(Money other) {
        return new Money(amount.subtract(other.amount).max(BigDecimal.ZERO));
    }

    public Money times(long multiplier) {
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public Money dividedBy(long divisor) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("divisor must be positive");
        }
        return new Money(amount.divide(BigDecimal.valueOf(divisor), SCALE, ROUNDING));
    }

    public Money percent(int percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("percentage must not be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(percentage)).divide(HUNDRED, SCALE, ROUNDING));
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }
}
