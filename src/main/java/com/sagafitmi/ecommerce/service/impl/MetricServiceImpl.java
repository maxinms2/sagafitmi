package com.sagafitmi.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.dto.MetricResponseDTO;
import com.sagafitmi.ecommerce.dto.OrderItemMetricDTO;
import com.sagafitmi.ecommerce.dto.ProductMetricDTO;
import com.sagafitmi.ecommerce.dto.ProductMetricRequest;
import com.sagafitmi.ecommerce.model.OrderItem;
import com.sagafitmi.ecommerce.repository.OrderItemRepository;
import com.sagafitmi.ecommerce.service.MetricService;
import com.sagafitmi.ecommerce.specification.OrderItemSpecification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MetricServiceImpl implements MetricService {

    private final OrderItemRepository orderItemRepository;

    public MetricServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MetricResponseDTO getMetricsOrders(MetricFilterRequest filter) {

        List<OrderItem> orderItems = orderItemRepository.findAll(OrderItemSpecification.byFilters(filter));

        List<OrderItemMetricDTO> rows = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem item : orderItems) {
            BigDecimal subtotal = getDto(rows, item);
            grandTotal = grandTotal.add(subtotal);
        }

        rows.sort(java.util.Comparator
            .comparing((OrderItemMetricDTO r) -> r.getProductName() == null ? "" : r.getProductName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing((OrderItemMetricDTO r) -> r.getProductDescription() == null ? "" : r.getProductDescription(), String.CASE_INSENSITIVE_ORDER));
        log.info("Calculated metrics: {} items, grand total {}", rows.size(), grandTotal);
        return MetricResponseDTO.builder().items(rows).grandTotal(grandTotal).build();
    }

    private BigDecimal getDto(List<OrderItemMetricDTO> rows, OrderItem item) {
        BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
        Integer qty = item.getQuantity() == null ? 0 : item.getQuantity();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

        OrderItemMetricDTO dto = OrderItemMetricDTO.builder()
                .orderId(item.getOrder() == null ? null : item.getOrder().getId())
                .orderStatus(item.getOrder() == null || item.getOrder().getStatus() == null ? null : item.getOrder().getStatus().name())
                .productName(item.getProduct() == null ? null : item.getProduct().getName())
                .productDescription(item.getProduct() == null ? null : item.getProduct().getDescription())
                .price(price)
                .quantity(qty)
                .subtotal(subtotal)
                .build();

        rows.add(dto);
        return subtotal;
    }

   @Override
    public List<ProductMetricDTO> getProductMetrics(ProductMetricRequest request) {
        // Use DB aggregation queries for performance (group + sum executed in DB)
        String sortBy = request.getSortBy() == null ? "quantity" : request.getSortBy().toLowerCase();
            int defaultTop = 10;
            Integer top = request.getTop();
            int size = (top == null || top <= 0) ? defaultTop : top;
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, size);

            if ("amount".equals(sortBy)) {
                return orderItemRepository.findProductMetricsOrderByAmountDesc(request.getStartDate(), request.getEndDate(), pageable);
            }
            return orderItemRepository.findProductMetricsOrderByQuantityDesc(request.getStartDate(), request.getEndDate(), pageable);
    }

}
