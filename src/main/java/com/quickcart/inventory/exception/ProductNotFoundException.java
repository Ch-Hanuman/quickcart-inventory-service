package com.quickcart.inventory.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product " + id + " not found");
    }
}
