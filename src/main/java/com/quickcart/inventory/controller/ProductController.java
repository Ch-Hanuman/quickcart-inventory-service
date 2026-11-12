package com.quickcart.inventory.controller;

import com.quickcart.inventory.dto.AddStockRequest;
import com.quickcart.inventory.dto.ProductRequest;
import com.quickcart.inventory.dto.ProductResponse;
import com.quickcart.inventory.model.Product;
import com.quickcart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService service;

    public ProductController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product p = service.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + p.getId())).body(ProductResponse.from(p));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(service.listProducts(name).stream().map(ProductResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ProductResponse.from(service.getProduct(id)));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> addStock(@PathVariable Long id, @Valid @RequestBody AddStockRequest request) {
        return ResponseEntity.ok(ProductResponse.from(service.addStock(id, request.quantity())));
    }
}
