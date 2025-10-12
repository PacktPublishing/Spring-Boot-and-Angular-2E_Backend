package com.packt.bookstore.inventory.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;

@Component
public class BookMapper {
    public Book toEntity(BookRequest req, Author author) {
        Book book = Book.builder()
                .title(req.title())
                .isbn(req.isbn())
                .author(author)
                .price(req.price())
                .genre(req.genre())
                .published(req.published())
                .description(req.description())
                .pageCount(req.pageCount())
                .coverImageUrl(req.coverImageUrl())
                .build();
        return book;
    }

    public BookResponse toResponse(Book b) {
        var authorName = (b.getAuthor() != null) ? b.getAuthor().getName() : null;
        return new BookResponse(
                 b.getId(),
                b.getTitle(),
                b.getIsbn(),
                authorName,
                b.getPrice(),
                b.getGenre(),
                b.getPublished(),
                b.getDescription(),
                b.getPageCount(),
                b.getCoverImageUrl()
        );
    }

    public void overwrite(Book target, BookRequest req, Author author) {
        target.setTitle(req.title());
        target.setIsbn(req.isbn());
        target.setAuthor(author);
        target.setPrice(req.price());
        target.setGenre(req.genre());
        target.setPublished(req.published());
        target.setDescription(req.description());
        target.setPageCount(req.pageCount());
        target.setCoverImageUrl(req.coverImageUrl());
    }

    public void patch(Book target, BookRequest req, @Nullable Author resolvedAuthor) {
        if (req.title() != null)
            target.setTitle(req.title());
        if (req.isbn() != null)
            target.setIsbn(req.isbn());
        if (resolvedAuthor != null)
            target.setAuthor(resolvedAuthor);
        target.setPrice(req.price());
        if (req.genre() != null)
            target.setGenre(req.genre());
        if (req.published() != null)
            target.setPublished(req.published());
        if (req.description() != null)
            target.setDescription(req.description());
        if (req.pageCount() != null)
            target.setPageCount(req.pageCount());
        if (req.coverImageUrl() != null)
            target.setCoverImageUrl(req.coverImageUrl());
    }



}
