package com.rupeek.hotelbooking.domain.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    private static final LocalDate DEC_10 = LocalDate.of(2026, 12, 10);

    @Test
    void occupiesEveryNightExceptTheCheckOutDate() {
        DateRange stay = new DateRange(DEC_10, DEC_10.plusDays(3));

        assertThat(stay.nightCount()).isEqualTo(3);
        assertThat(stay.nights()).containsExactly(DEC_10, DEC_10.plusDays(1), DEC_10.plusDays(2));
    }

    @Test
    void rejectsZeroNightAndInvertedStays() {
        assertThatThrownBy(() -> new DateRange(DEC_10, DEC_10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DateRange(DEC_10, DEC_10.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresBothDates() {
        assertThatThrownBy(() -> new DateRange(null, DEC_10)).isInstanceOf(IllegalArgumentException.class);
    }
}
