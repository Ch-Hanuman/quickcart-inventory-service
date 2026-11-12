package com.quickcart.inventory.exception;

import org.springframework.http.HttpStatus;

public class DuplicateSkuException extends BusinessException {
    public DuplicateSkuException(String sku) {
        super(HttpStatus.CONFLICT, "DUPLICATE_SKU", "A product with SKU '" + sku + "' already exists");
    }
}
