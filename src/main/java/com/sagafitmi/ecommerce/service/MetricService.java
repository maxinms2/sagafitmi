package com.sagafitmi.ecommerce.service;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.dto.ProductMetricRequest;
import com.sagafitmi.ecommerce.dto.MetricResponseDTO;

public interface MetricService {
    MetricResponseDTO getMetricsOrders(MetricFilterRequest filter);
    java.util.List<com.sagafitmi.ecommerce.dto.ProductMetricDTO> getProductMetrics(ProductMetricRequest request);
}
