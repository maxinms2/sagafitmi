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
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_name", columnList = "name"),
    @Index(name = "idx_products_description", columnList = "description")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"prices", "images"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Price> prices = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

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

    // Helpers para mantener la relación bidireccional
    public void addImage(ProductImage image) {
        if (image == null) return;
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        if (image == null) return;
        images.remove(image);
        image.setProduct(null);
    }
}
