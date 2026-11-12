package com.quickcart.inventory.config;

import com.quickcart.inventory.model.Product;
import com.quickcart.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    new Product("MILK-1L", "Full Cream Milk 1L", "Fresh pasteurised milk", new BigDecimal("62.00"), 100),
                    new Product("RICE-5KG", "Basmati Rice 5kg", "Long grain basmati", new BigDecimal("640.00"), 40),
                    new Product("EGGS-12", "Eggs (12 pack)", "Farm eggs", new BigDecimal("96.00"), 60),
                    new Product("BREAD-WH", "Whole Wheat Bread", "Sliced loaf 400g", new BigDecimal("45.00"), 80),
                    new Product("APPLE-1KG", "Apples 1kg", "Kashmir apples", new BigDecimal("180.00"), 25)));
        };
    }
}
