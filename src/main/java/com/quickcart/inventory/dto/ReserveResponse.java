package com.quickcart.inventory.dto;

import java.util.List;

public record ReserveResponse(Long orderId, String status, List<ReservedLine> lines) { }
