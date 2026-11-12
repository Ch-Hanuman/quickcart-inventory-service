package com.quickcart.inventory.controller;

import com.quickcart.inventory.dto.ReserveRequest;
import com.quickcart.inventory.dto.ReserveResponse;
import com.quickcart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse> reserve(@Valid @RequestBody ReserveRequest request) {
        return ResponseEntity.ok(service.reserve(request));
    }

    @PostMapping("/release/{orderId}")
    public ResponseEntity<Void> release(@PathVariable Long orderId) {
        service.release(orderId);
        return ResponseEntity.noContent().build();
    }
}
