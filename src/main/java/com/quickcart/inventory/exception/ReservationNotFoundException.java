package com.quickcart.inventory.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends BusinessException {
    public ReservationNotFoundException(Long orderId) {
        super(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "No reservation for order " + orderId);
    }
}
