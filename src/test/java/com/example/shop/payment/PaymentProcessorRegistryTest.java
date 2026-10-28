package com.example.shop.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentProcessorRegistryTest {

    private static PaymentProcessor stub(String code) {
        return new PaymentProcessor() {
            @Override
            public String method() {
                return code;
            }

            @Override
            public PaymentResult pay(PaymentCommand command) {
                return new PaymentResult.Approved(code + "-OK");
            }
        };
    }

    @Test
    void forMethod_isCaseInsensitive() {
        PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(stub("CARD")));

        assertThat(registry.forMethod("card").method()).isEqualTo("CARD");
    }

    @Test
    void forMethod_unknownMethod_listsSupportedOnes() {
        PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(stub("CARD"), stub("UPI")));

        assertThatThrownBy(() -> registry.forMethod("BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BITCOIN")
                .hasMessageContaining("CARD")
                .hasMessageContaining("UPI");
    }

    @Test
    void duplicateMethodCodes_failFast() {
        assertThatThrownBy(() -> new PaymentProcessorRegistry(List.of(stub("CARD"), stub("card"))))
                .isInstanceOf(IllegalStateException.class);
    }

    /** The Open/Closed principle as a test: a brand new method works with no change to the registry or the service. */
    @Test
    void newProcessor_isUsableWithoutChangingAnythingElse() {
        PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(stub("CARD"), stub("NET_BANKING")));
        PaymentService service = new PaymentService(registry);

        PaymentResult result = service.pay(new PaymentCommand(1L, com.example.shop.common.Money.of("10"), "NET_BANKING", null));

        assertThat(result).isEqualTo(new PaymentResult.Approved("NET_BANKING-OK"));
        assertThat(registry.supportedMethods()).containsExactly("CARD", "NET_BANKING");
    }
}
