package com.quickcart.inventory.dto;

import java.math.BigDecimal;

public record ReservedLine(Long productId, String productName, int quantity, BigDecimal unitPrice) { }
