package com.quickcart.inventory.dto;

import com.quickcart.inventory.model.Product;

import java.math.BigDecimal;

public record ProductResponse(Long id, String sku, String name, String description,
                              BigDecimal price, int availableQuantity) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getPrice(), p.getAvailableQuantity());
    }
}
