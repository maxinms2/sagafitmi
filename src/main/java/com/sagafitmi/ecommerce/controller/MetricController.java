package com.sagafitmi.ecommerce.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.dto.MetricResponseDTO;
import com.sagafitmi.ecommerce.service.MetricService;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalProducts", 0);
        metrics.put("totalUsers", 0);
        metrics.put("totalOrders", 0);
        metrics.put("uptimeSeconds", 0);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getMetricByName(@PathVariable String name) {
        Map<String, Object> metric = new HashMap<>();
        // Valores de ejemplo; reemplace con lógica real si se requiere
        metric.put("name", name);
        metric.put("value", 0);
        return ResponseEntity.ok(metric);
    }

    @PostMapping("/orders")
    public ResponseEntity<MetricResponseDTO> queryMetrics(@RequestBody MetricFilterRequest filter) {
        if (filter.getStartDate() != null) {
            filter.setStartDate(filter.getStartDate()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0));
        }

        if (filter.getEndDate() != null) {
            filter.setEndDate(filter.getEndDate()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_000_000));
        }
        MetricResponseDTO response = metricService.getMetrics(filter);
        return ResponseEntity.ok(response);
    }

}
