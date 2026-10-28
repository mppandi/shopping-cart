package com.example.shop.product;

import com.example.shop.common.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    /** DTO: the API shape is separate from the domain shape. */
    public record ProductResponse(long id, String name, BigDecimal price) {
        static ProductResponse from(Product p) {
            return new ProductResponse(p.id(), p.name(), p.price().amount());
        }
    }

    private final ProductCatalog catalog;

    public ProductController(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<ProductResponse> list() {
        return catalog.findAll().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable long id) {
        return catalog.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }
}
