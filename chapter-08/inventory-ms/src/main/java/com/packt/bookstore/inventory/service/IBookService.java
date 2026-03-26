package com.packt.bookstore.inventory.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;

public interface IBookService {
    List<BookResponse> findAll(int page, int size, String sort);

    Page<BookResponse> findAllPaginated(int page, int size);

    BookResponse findOne(Long id);

    BookResponse create(BookRequest req);

    BookResponse replace(Long id, BookRequest req);

    BookResponse patch(Long id, BookRequest req);

    void delete(Long id);

}
