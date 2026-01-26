package com.packt.bookstore.inventory.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record BookRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN must be 10-17 chars/dashes")
        String isbn,

        String authorName,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be >= 0")
        BigDecimal price,

        String genre,
        LocalDate published,
        String description,
        Integer pageCount,
        String coverImageUrl
) {}
