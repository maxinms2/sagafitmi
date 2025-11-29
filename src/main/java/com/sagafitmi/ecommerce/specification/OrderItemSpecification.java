package com.sagafitmi.ecommerce.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sagafitmi.ecommerce.dto.MetricFilterRequest;
import com.sagafitmi.ecommerce.model.OrderItem;
import com.sagafitmi.ecommerce.model.OrderStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class OrderItemSpecification {

    public static Specification<OrderItem> byFilters(MetricFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            getDatesPredicates(filter, root, cb, predicates);
            getStatusPredicates(filter, root, predicates);
            getUsersPredicates(filter, root, predicates);
            getProductsIdsPredicates(filter, predicates, root);
            getProductDescriptionsPredicates(filter, cb, predicates, root);
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void getDatesPredicates(MetricFilterRequest filter, Root<OrderItem> root, CriteriaBuilder cb,
            List<Predicate> predicates) {
        if (filter.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("order").get("createdAt"), filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("order").get("createdAt"), filter.getEndDate()));
        }
    }

    private static void getStatusPredicates(MetricFilterRequest filter, Root<OrderItem> root, List<Predicate> predicates) {
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            List<OrderStatus> enums = filter.getStatuses().stream().map(s -> {
                try { return OrderStatus.valueOf(s.toUpperCase()); } catch (Exception ex) { return null; }
            }).filter(e -> e != null).collect(Collectors.toList());
            if (!enums.isEmpty()) {
                predicates.add(root.get("order").get("status").in(enums));
            }
        }
    }

    private static void getUsersPredicates(MetricFilterRequest filter, Root<OrderItem> root, List<Predicate> predicates) {
        if (filter.getUserIds() != null && !filter.getUserIds().isEmpty()) {
            predicates.add(root.get("order").get("user").get("id").in(filter.getUserIds()));
        }
    }

    private static void getProductsIdsPredicates(MetricFilterRequest filter, List<Predicate> predicates,
            Root<OrderItem> root) {
        if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
            predicates.add(root.get("product").get("id").in(filter.getProductIds()));
        }
    }

    private static void getProductDescriptionsPredicates(MetricFilterRequest filter, CriteriaBuilder cb, List<Predicate> predicates,
            Root<OrderItem> root) {
        if (filter.getProductDescriptions() != null && !filter.getProductDescriptions().isEmpty()) {
            predicates.add(root.get("product").get("description").in(filter.getProductDescriptions()));
        }
    }
}
