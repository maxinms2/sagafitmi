package com.sagafitmi.ecommerce.service;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.dto.MetricResponseDTO;

public interface MetricService {
    MetricResponseDTO getMetrics(MetricFilterRequest filter);
}
