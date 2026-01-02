package com.packt.bookstore.inventory.mapper;

import org.springframework.stereotype.Component;

import com.packt.bookstore.inventory.dto.AuthorRequest;
import com.packt.bookstore.inventory.dto.AuthorResponse;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;

@Component
public class AuthorMapper {

    public Author toEntity(AuthorRequest req) {
        return Author.builder()
                .name(req.name())
                .nationality(req.nationality())
                .build();
    }

  public AuthorResponse toResponse(Author author) {
    return new AuthorResponse(
        author.getName(),
        author.getNationality(),
        author.getBooks() != null
            ? author.getBooks().stream()
                .map(Book::getTitle)
                .toList()
            : null
    );
}
}
