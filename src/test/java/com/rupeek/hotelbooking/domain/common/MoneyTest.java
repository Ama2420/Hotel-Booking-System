package com.rupeek.hotelbooking.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalisesToTwoDecimalPlacesSoEqualAmountsAreEqual() {
        assertThat(Money.of("100")).isEqualTo(Money.of("100.00"));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> Money.of("-1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void computesPercentagesAndDivisionWithBankersRounding() {
        assertThat(Money.of("4500").percent(20)).isEqualTo(Money.of("900"));
        assertThat(Money.of("100").dividedBy(3)).isEqualTo(Money.of("33.33"));
    }

    @Test
    void subtractionNeverGoesBelowZero() {
        assertThat(Money.of("100").minusFloorZero(Money.of("250"))).isEqualTo(Money.ZERO);
        assertThat(Money.of("250").minusFloorZero(Money.of("100"))).isEqualTo(Money.of("150"));
    }
}
