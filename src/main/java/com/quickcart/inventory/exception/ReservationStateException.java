package com.quickcart.inventory.exception;

import org.springframework.http.HttpStatus;

public class ReservationStateException extends BusinessException {
    public ReservationStateException(Long orderId, String state) {
        super(HttpStatus.CONFLICT, "RESERVATION_STATE", "Reservation for order " + orderId + " is already " + state);
    }
}
