package com.quickcart.inventory.service;

import com.quickcart.inventory.dto.ItemRequest;
import com.quickcart.inventory.dto.ReserveRequest;
import com.quickcart.inventory.dto.ReserveResponse;
import com.quickcart.inventory.exception.InsufficientStockException;
import com.quickcart.inventory.model.Product;
import com.quickcart.inventory.model.Reservation;
import com.quickcart.inventory.model.ReservationStatus;
import com.quickcart.inventory.model.ReservedItem;
import com.quickcart.inventory.repository.ProductRepository;
import com.quickcart.inventory.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks InventoryService service;

    private Product product(long id, int stock) {
        Product p = new Product("SKU-" + id, "Product " + id, "desc", new BigDecimal("10.00"), stock);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    @Test
    void reserve_decrementsStockAndReturnsPricedLines() {
        Product p = product(1L, 10);
        when(reservationRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ReserveResponse res = service.reserve(new ReserveRequest(100L, List.of(new ItemRequest(1L, 3))));

        assertEquals(7, p.getAvailableQuantity());
        assertEquals("RESERVED", res.status());
        assertEquals(1, res.lines().size());
        assertEquals(0, new BigDecimal("10.00").compareTo(res.lines().get(0).unitPrice()));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserve_mergesDuplicateLines() {
        Product p = product(1L, 10);
        when(reservationRepository.findByOrderId(101L)).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        service.reserve(new ReserveRequest(101L, List.of(new ItemRequest(1L, 2), new ItemRequest(1L, 3))));

        assertEquals(5, p.getAvailableQuantity());
    }

    @Test
    void reserve_throwsWhenStockInsufficient_andSavesNothing() {
        Product p = product(1L, 2);
        when(reservationRepository.findByOrderId(102L)).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        assertThrows(InsufficientStockException.class,
                () -> service.reserve(new ReserveRequest(102L, List.of(new ItemRequest(1L, 5)))));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserve_isIdempotentForSameOrder() {
        Product p = product(1L, 7);
        Reservation existing = new Reservation(103L);
        existing.addItem(new ReservedItem(1L, 3));
        when(reservationRepository.findByOrderId(103L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        ReserveResponse res = service.reserve(new ReserveRequest(103L, List.of(new ItemRequest(1L, 3))));

        assertEquals("RESERVED", res.status());
        assertEquals(7, p.getAvailableQuantity());
        verify(productRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void release_returnsStockAndMarksReleased() {
        Product p = product(1L, 7);
        Reservation r = new Reservation(104L);
        r.addItem(new ReservedItem(1L, 3));
        when(reservationRepository.findByOrderId(104L)).thenReturn(Optional.of(r));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        service.release(104L);

        assertEquals(10, p.getAvailableQuantity());
        assertEquals(ReservationStatus.RELEASED, r.getStatus());
    }
}
