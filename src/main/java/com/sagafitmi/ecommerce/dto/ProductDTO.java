package com.sagafitmi.ecommerce.dto;

import java.util.List;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    // current price as double for API compatibility (nullable)
    private Double price;
    // optional price history
    private List<PriceDTO> priceHistory;

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
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public List<PriceDTO> getPriceHistory() {
        return priceHistory;
    }
    public void setPriceHistory(List<PriceDTO> priceHistory) {
        this.priceHistory = priceHistory;
    }
}
