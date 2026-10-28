package com.example.shop.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    public record AddItemRequest(
            @Positive long productId,
            @Min(1) @Max(CartItem.MAX_QUANTITY) int quantity) {}

    public record ItemResponse(long productId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}

    public record CartResponse(List<ItemResponse> items, BigDecimal total) {
        static CartResponse from(Cart cart) {
            List<ItemResponse> items = cart.items().stream()
                    .map(i -> new ItemResponse(
                            i.productId(), i.name(), i.unitPrice().amount(), i.quantity(), i.lineTotal().amount()))
                    .toList();
            return new CartResponse(items, cart.total().amount());
        }
    }

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    /** The logged-in user comes from the JWT: a user can only ever see and change their own cart. */
    @GetMapping
    public CartResponse view(Principal principal) {
        return CartResponse.from(service.view(principal.getName()));
    }

    @PostMapping("/items")
    public CartResponse addItem(Principal principal, @Valid @RequestBody AddItemRequest request) {
        return CartResponse.from(service.addItem(principal.getName(), request.productId(), request.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(Principal principal, @PathVariable long productId) {
        return CartResponse.from(service.removeItem(principal.getName(), productId));
    }
}
