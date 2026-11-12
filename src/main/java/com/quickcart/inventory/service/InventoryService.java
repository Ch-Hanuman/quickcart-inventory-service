package com.quickcart.inventory.service;

import com.quickcart.inventory.dto.ProductRequest;
import com.quickcart.inventory.dto.ReserveRequest;
import com.quickcart.inventory.dto.ReserveResponse;
import com.quickcart.inventory.dto.ReservedLine;
import com.quickcart.inventory.exception.DuplicateSkuException;
import com.quickcart.inventory.exception.InsufficientStockException;
import com.quickcart.inventory.exception.ProductNotFoundException;
import com.quickcart.inventory.exception.ReservationNotFoundException;
import com.quickcart.inventory.exception.ReservationStateException;
import com.quickcart.inventory.model.Product;
import com.quickcart.inventory.model.Reservation;
import com.quickcart.inventory.model.ReservationStatus;
import com.quickcart.inventory.model.ReservedItem;
import com.quickcart.inventory.repository.ProductRepository;
import com.quickcart.inventory.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(ProductRepository productRepository, ReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Product createProduct(ProductRequest r) {
        if (productRepository.existsBySku(r.sku())) {
            throw new DuplicateSkuException(r.sku());
        }
        return productRepository.save(new Product(r.sku(), r.name(), r.description(), r.price(), r.initialStock()));
    }

    @Transactional
    public Product addStock(Long productId, int quantity) {
        Product p = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        p.setAvailableQuantity(p.getAvailableQuantity() + quantity);
        return p;
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest req) {
        Optional<Reservation> existing = reservationRepository.findByOrderId(req.orderId());
        if (existing.isPresent()) {
            Reservation r = existing.get();
            if (r.getStatus() != ReservationStatus.RESERVED) {
                throw new ReservationStateException(req.orderId(), r.getStatus().name());
            }
            return toResponse(r);
        }

        Map<Long, Integer> wanted = new TreeMap<>();
        req.items().forEach(i -> wanted.merge(i.productId(), i.quantity(), Integer::sum));

        Reservation reservation = new Reservation(req.orderId());
        for (Map.Entry<Long, Integer> e : wanted.entrySet()) {
            Product p = productRepository.findByIdForUpdate(e.getKey())
                    .orElseThrow(() -> new ProductNotFoundException(e.getKey()));
            if (p.getAvailableQuantity() < e.getValue()) {
                throw new InsufficientStockException(p.getId(), p.getAvailableQuantity(), e.getValue());
            }
            p.setAvailableQuantity(p.getAvailableQuantity() - e.getValue());
            reservation.addItem(new ReservedItem(p.getId(), e.getValue()));
        }
        reservationRepository.save(reservation);
        log.info("Reserved stock for order {} ({} products)", req.orderId(), wanted.size());
        return toResponse(reservation);
    }

    @Transactional
    public void release(Long orderId) {
        Reservation r = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ReservationNotFoundException(orderId));
        if (r.getStatus() == ReservationStatus.RELEASED) {
            return;
        }
        r.getItems().stream()
                .sorted(Comparator.comparing(ReservedItem::getProductId))
                .forEach(item -> {
                    Product p = productRepository.findByIdForUpdate(item.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                    p.setAvailableQuantity(p.getAvailableQuantity() + item.getQuantity());
                });
        r.setStatus(ReservationStatus.RELEASED);
        log.info("Released stock for order {}", orderId);
    }


    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts(String nameFilter) {
        return nameFilter == null || nameFilter.isBlank()
                ? productRepository.findAll(Sort.by("name"))
                : productRepository.findByNameContainingIgnoreCaseOrderByName(nameFilter);
    }

    private ReserveResponse toResponse(Reservation r) {
        List<ReservedLine> lines = r.getItems().stream()
                .sorted(Comparator.comparing(ReservedItem::getProductId))
                .map(i -> {
                    Product p = productRepository.findById(i.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(i.getProductId()));
                    return new ReservedLine(p.getId(), p.getName(), i.getQuantity(), p.getPrice());
                })
                .toList();
        return new ReserveResponse(r.getOrderId(), r.getStatus().name(), lines);
    }
}
