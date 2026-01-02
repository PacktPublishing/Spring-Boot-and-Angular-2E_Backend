package com.packt.bookstore.inventory.dto;

import java.util.List;

public record AuthorResponse(
        String name,
        String nationality,
        List<String> books
) {

}
