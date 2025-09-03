package com.packt.bookstore.inventory.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record BookResponse(
                Long id,
                String title,
                String isbn,
                String authorName,
                BigDecimal price,
                String genre,
                String published,
                String description,
                Integer pageCount,
                String coverImageUrl
) {
}
