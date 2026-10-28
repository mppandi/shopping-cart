package com.example.shop.cart;

import com.example.shop.common.NotFoundException;
import com.example.shop.product.Product;
import com.example.shop.product.ProductCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cart use cases. Depends only on ports (CartRepository, ProductCatalog), so it is easy to test with fakes. */
@Service
public class CartService {

    private final CartRepository carts;
    private final ProductCatalog catalog;

    public CartService(CartRepository carts, ProductCatalog catalog) {
        this.carts = carts;
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public Cart view(String owner) {
        return carts.findByOwner(owner).orElseGet(() -> Cart.emptyFor(owner));
    }

    @Transactional
    public Cart addItem(String owner, long productId, int quantity) {
        Product product = catalog.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product " + productId + " not found"));
        CartItem item = new CartItem(product.id(), product.name(), product.price(), quantity);
        return carts.save(view(owner).add(item));
    }

    @Transactional
    public Cart removeItem(String owner, long productId) {
        return carts.save(view(owner).remove(productId));
    }
}
