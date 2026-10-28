package com.example.shop.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * LISKOV SUBSTITUTION as a test: every PaymentProcessor must pass these checks, so any of them
 * can be used wherever a PaymentProcessor is expected. A new processor extends this class and supplies two commands.
 */
abstract class PaymentProcessorContractTest {

    protected abstract PaymentProcessor processor();

    protected abstract PaymentCommand validCommand();

    protected abstract PaymentCommand commandWithMissingDetails();

    @Test
    void method_isNotBlank() {
        assertThat(processor().method()).isNotBlank();
    }

    @Test
    void pay_validCommand_returnsAResult() {
        assertThat(processor().pay(validCommand())).isNotNull();
    }

    @Test
    void pay_sameCommandTwice_givesTheSameResult() {
        PaymentResult first = processor().pay(validCommand());

        assertThat(processor().pay(validCommand())).isEqualTo(first);
    }

    @Test
    void pay_missingDetails_isRejectedWithAClearMessage() {
        assertThatThrownBy(() -> processor().pay(commandWithMissingDetails()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(processor().method());
    }
}
