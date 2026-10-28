package com.example.shop.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void constructor_roundsToTwoDecimals() {
        assertThat(new Money(new BigDecimal("10.005")).amount()).isEqualByComparingTo("10.01");
    }

    @Test
    void constructor_rejectsNegativeAmounts() {
        assertThatThrownBy(() -> Money.of("-0.01")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void plus_and_times_returnNewValues() {
        Money ten = Money.of("10.00");
        assertThat(ten.plus(Money.of("2.50"))).isEqualTo(Money.of("12.50"));
        assertThat(ten.times(3)).isEqualTo(Money.of("30.00"));
        assertThat(ten).isEqualTo(Money.of("10")); // original unchanged
    }
}
