package com.splashcan.backend.product;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> hasCategorySlug(String slug) {
        return (root, query, cb) -> slug == null ? null : cb.equal(root.get("category").get("slug"), slug);
    }

    public static Specification<Product> hasFlavor(String flavor) {
        return (root, query, cb) -> {
            if (flavor == null) {
                return null;
            }
            query.distinct(true);
            Join<Object, Object> variants = root.join("variants", JoinType.INNER);
            return cb.equal(variants.get("flavor"), flavor);
        };
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("basePrice"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("basePrice"), min);
            }
            if (max != null) {
                return cb.lessThanOrEqualTo(root.get("basePrice"), max);
            }
            return null;
        };
    }
}
