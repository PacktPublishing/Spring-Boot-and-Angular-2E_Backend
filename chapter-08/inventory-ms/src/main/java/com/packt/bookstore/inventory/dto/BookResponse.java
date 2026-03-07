package com.packt.bookstore.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookResponse(
                Long id,
                String title,
                String isbn,
                AuthorResponse author,
                BigDecimal price,
                String genre,
                LocalDate published,
                String description,
                Integer pageCount,
                String coverImageUrl
) {
}
