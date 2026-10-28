package com.example.shop.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CheckoutRequest(@NotBlank String paymentMethod, Map<String, String> paymentDetails) {}

    public record LineResponse(long productId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}

    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, Instant placedAt, List<LineResponse> lines) {
        static OrderResponse from(Order o) {
            return new OrderResponse(
                    o.id(),
                    o.status(),
                    o.total().amount(),
                    o.placedAt(),
                    o.lines().stream()
                            .map(l -> new LineResponse(
                                    l.productId(), l.name(), l.unitPrice().amount(), l.quantity(), l.lineTotal().amount()))
                            .toList());
        }
    }

    private final CheckoutService checkoutService;

    public OrderController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /** 201 Created when paid or awaiting confirmation, 402 Payment Required when the payment is declined. */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(Principal principal, @Valid @RequestBody CheckoutRequest request) {
        Order order = checkoutService.checkout(principal.getName(), request.paymentMethod(), request.paymentDetails());
        HttpStatus status = order.status() == OrderStatus.PAYMENT_FAILED ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(OrderResponse.from(order));
    }

    @GetMapping
    public List<OrderResponse> list(Principal principal) {
        return checkoutService.list(principal.getName()).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse get(Principal principal, @PathVariable long id) {
        return OrderResponse.from(checkoutService.get(principal.getName(), id));
    }
}
