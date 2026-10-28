package com.example.shop.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.common.Money;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UpiPaymentProcessorTest extends PaymentProcessorContractTest {

    private final UpiPaymentProcessor processor = new UpiPaymentProcessor();

    @Override
    protected PaymentProcessor processor() {
        return processor;
    }

    @Override
    protected PaymentCommand validCommand() {
        return new PaymentCommand(8, Money.of("20.00"), "UPI", Map.of("vpa", "alice@bank"));
    }

    @Override
    protected PaymentCommand commandWithMissingDetails() {
        return new PaymentCommand(8, Money.of("20.00"), "UPI", Map.of("vpa", "not-a-vpa"));
    }

    @Test
    void validVpa_returnsPending() {
        assertThat(processor.pay(validCommand())).isEqualTo(new PaymentResult.Pending("UPI-REQ-8"));
    }
}
