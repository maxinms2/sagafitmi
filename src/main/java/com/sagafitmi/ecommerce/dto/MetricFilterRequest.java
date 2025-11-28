package com.sagafitmi.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricFilterRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> productIds;
    private List<String> productDescriptions;
    private List<String> statuses;
    private List<Long> userIds;
}
