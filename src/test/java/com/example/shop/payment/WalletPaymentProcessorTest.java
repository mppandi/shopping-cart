package com.example.shop.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.common.Money;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WalletPaymentProcessorTest extends PaymentProcessorContractTest {

    private final WalletPaymentProcessor processor = new WalletPaymentProcessor();

    @Override
    protected PaymentProcessor processor() {
        return processor;
    }

    @Override
    protected PaymentCommand validCommand() {
        return new PaymentCommand(9, Money.of("100.00"), "WALLET", Map.of("walletId", "w-1"));
    }

    @Override
    protected PaymentCommand commandWithMissingDetails() {
        return new PaymentCommand(9, Money.of("100.00"), "WALLET", Map.of());
    }

    @Test
    void withinBalance_isApproved() {
        assertThat(processor.pay(validCommand())).isEqualTo(new PaymentResult.Approved("WALLET-9"));
    }

    @Test
    void aboveBalance_isDeclined() {
        PaymentCommand big = new PaymentCommand(9, Money.of("500.01"), "WALLET", Map.of("walletId", "w-1"));

        assertThat(processor.pay(big)).isEqualTo(new PaymentResult.Declined("Insufficient wallet balance"));
    }
}
