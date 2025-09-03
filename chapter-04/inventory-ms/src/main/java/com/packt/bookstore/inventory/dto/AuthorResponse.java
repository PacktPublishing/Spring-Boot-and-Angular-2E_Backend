package com.packt.bookstore.inventory.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record AuthorResponse(
        String name,
        String nationality,
        List<String> books
) {

}
