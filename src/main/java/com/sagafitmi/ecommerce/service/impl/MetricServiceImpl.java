package com.sagafitmi.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.dto.MetricResponseDTO;
import com.sagafitmi.ecommerce.dto.OrderItemMetricDTO;
import com.sagafitmi.ecommerce.model.Order;
import com.sagafitmi.ecommerce.model.OrderItem;
import com.sagafitmi.ecommerce.model.Product;
import com.sagafitmi.ecommerce.repository.OrderRepository;
import com.sagafitmi.ecommerce.service.MetricService;
import com.sagafitmi.ecommerce.specification.OrderSpecification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MetricServiceImpl implements MetricService {

    private final OrderRepository orderRepository;

    public MetricServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MetricResponseDTO getMetrics(MetricFilterRequest filter) {

        List<Order> orders = orderRepository.findAll(OrderSpecification.byFilters(filter));

        List<OrderItemMetricDTO> rows = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        boolean filterByProduct = (filter.getProductIds() != null && !filter.getProductIds().isEmpty())
                || (filter.getProductDescriptions() != null && !filter.getProductDescriptions().isEmpty());

        for (Order order : orders) {
            List<OrderItem> items = order.getItems();
            List<OrderItem> filteredItems = items;
            if (filterByProduct) {
                filteredItems = filterProducts(filter, items);
            }

            for (OrderItem item : filteredItems) {
                BigDecimal subtotal = getDto(rows, order, item);
                grandTotal = grandTotal.add(subtotal);
            }
        }
        rows.sort(java.util.Comparator
            .comparing((OrderItemMetricDTO r) -> r.getProductName() == null ? "" : r.getProductName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing((OrderItemMetricDTO r) -> r.getProductDescription() == null ? "" : r.getProductDescription(), String.CASE_INSENSITIVE_ORDER));
        log.info("Calculated metrics: {} items, grand total {}", rows.size(), grandTotal);
        return MetricResponseDTO.builder().items(rows).grandTotal(grandTotal).build();
    }

    private List<OrderItem> filterProducts(MetricFilterRequest filter, List<OrderItem> items) {
        List<OrderItem> filteredItems;
        filteredItems = items.stream().filter(it -> {
            Product p = it.getProduct();
            boolean ok = true;
            if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
                ok = ok && filter.getProductIds().contains(p.getId());
            }
            if (filter.getProductDescriptions() != null && !filter.getProductDescriptions().isEmpty()) {
                String desc = p.getDescription() == null ? "" : p.getDescription().toLowerCase();
                boolean any = filter.getProductDescriptions().stream().filter(Objects::nonNull)
                        .map(String::toLowerCase)
                        .anyMatch(d -> desc.contains(d));
                ok = ok && any;
            }
            return ok;
        }).collect(Collectors.toList());
        return filteredItems;
    }

    private BigDecimal getDto(List<OrderItemMetricDTO> rows, Order order, OrderItem item) {
        BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
        Integer qty = item.getQuantity() == null ? 0 : item.getQuantity();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

        OrderItemMetricDTO dto = OrderItemMetricDTO.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus() == null ? null : order.getStatus().name())
                .productName(item.getProduct() == null ? null : item.getProduct().getName())
                .productDescription(item.getProduct() == null ? null : item.getProduct().getDescription())
                .price(price)
                .quantity(qty)
                .subtotal(subtotal)
                .build();

        rows.add(dto);
        return subtotal;
    }

}
