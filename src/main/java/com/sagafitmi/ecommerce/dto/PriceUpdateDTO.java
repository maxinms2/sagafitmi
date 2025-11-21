package com.sagafitmi.ecommerce.dto;

public class PriceUpdateDTO {
    private Double price;

    public PriceUpdateDTO() {}

    public PriceUpdateDTO(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
