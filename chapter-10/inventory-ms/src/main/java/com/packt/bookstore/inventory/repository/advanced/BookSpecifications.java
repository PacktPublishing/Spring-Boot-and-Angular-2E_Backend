package com.packt.bookstore.inventory.repository.advanced;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.packt.bookstore.inventory.entity.Book;

public class BookSpecifications {
    public static Specification<Book> hasTitleLike(String keyword) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Book> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }
}
