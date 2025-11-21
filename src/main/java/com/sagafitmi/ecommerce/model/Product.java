package com.sagafitmi.ecommerce.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Price> prices = new ArrayList<>();

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<Price> getPrices() {
        return prices;
    }
    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    // Conveniencia: obtener el precio más reciente (por createdAt)
    public Price getCurrentPrice() {
        if (prices == null || prices.isEmpty()) return null;
        return prices.stream()
                .filter(p -> p.getCreatedAt() != null)
                .max(Comparator.comparing(Price::getCreatedAt))
                .orElse(prices.get(prices.size() - 1));
    }

    public BigDecimal getCurrentPriceValue() {
        Price p = getCurrentPrice();
        return p != null ? p.getPrice() : null;
    }
}
