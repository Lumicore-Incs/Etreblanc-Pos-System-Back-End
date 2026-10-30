package com.selling.util;

import com.selling.model.Order;
import com.selling.model.OrderDetails;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> filterOrders(
            String search,
            String status,
            Integer productId,
            LocalDate filterDate,
            boolean todayOnly,
            Integer userId) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            Join<Object, Object> customer =
                    root.join("customer", JoinType.INNER);

            predicates.add(cb.isNotNull(root.get("date")));

            // User filter
            if (userId != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), userId)
                );
            }

            // Search
            if (search != null && !search.trim().isEmpty()) {

                String keyword =
                        "%" + search.trim().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(customer.get("name")), keyword),
                                cb.like(cb.lower(root.get("weyBillId")), keyword),
                                cb.like(cb.lower(customer.get("contact01")), keyword),
                                cb.like(cb.lower(customer.get("contact02")), keyword)
                        )
                );
            }

            // Status
            if (status != null
                    && !status.trim().isEmpty()
                    && !status.trim().equalsIgnoreCase("ALL STATUS")) {

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("status")),
                                status.trim().toLowerCase()
                        )
                );
            }

            // Date range
            if (filterDate != null) {

                LocalDateTime start = filterDate.atStartOfDay();
                LocalDateTime end = filterDate.plusDays(1).atStartOfDay();

                predicates.add(
                        cb.and(
                                cb.greaterThanOrEqualTo(root.get("date"), start),
                                cb.lessThan(root.get("date"), end)
                        )
                );
            }

            // Today only
            if (todayOnly) {

                LocalDate today = LocalDate.now();

                predicates.add(
                        cb.and(
                                cb.greaterThanOrEqualTo(
                                        root.get("date"),
                                        today.atStartOfDay()
                                ),
                                cb.lessThan(
                                        root.get("date"),
                                        today.plusDays(1).atStartOfDay()
                                )
                        )
                );
            }

            // Product filter
            if (productId != null) {

                var subquery = query.subquery(Integer.class);
                var details = subquery.from(OrderDetails.class);

                subquery.select(details.get("order").get("orderId"));

                subquery.where(
                        cb.and(
                                cb.equal(
                                        details.get("order").get("orderId"),
                                        root.get("orderId")
                                ),
                                cb.equal(
                                        details.get("product").get("productId"),
                                        productId
                                )
                        )
                );

                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}