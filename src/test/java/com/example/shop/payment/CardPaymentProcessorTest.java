package com.example.shop.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.common.Money;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CardPaymentProcessorTest extends PaymentProcessorContractTest {

    private final CardPaymentProcessor processor = new CardPaymentProcessor();

    @Override
    protected PaymentProcessor processor() {
        return processor;
    }

    @Override
    protected PaymentCommand validCommand() {
        return new PaymentCommand(7, Money.of("50.00"), "CARD", Map.of("cardToken", "tok_ok"));
    }

    @Override
    protected PaymentCommand commandWithMissingDetails() {
        return new PaymentCommand(7, Money.of("50.00"), "CARD", Map.of());
    }

    @Test
    void validToken_isApproved() {
        assertThat(processor.pay(validCommand())).isEqualTo(new PaymentResult.Approved("CARD-7"));
    }

    @Test
    void failToken_isDeclined() {
        PaymentCommand command = new PaymentCommand(7, Money.of("50.00"), "CARD", Map.of("cardToken", "tok_fail_1"));

        assertThat(processor.pay(command)).isInstanceOf(PaymentResult.Declined.class);
    }
}
