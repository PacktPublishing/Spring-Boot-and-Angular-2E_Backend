package com.packt.bookstore.inventory.repository.advanced;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.bookstore.inventory.entity.Book;

public interface ReadOnlyBookRepository
        extends JpaRepository<Book, Long> {
    @Override
    @Deprecated
    <S extends Book> S save(S entity); // disabled

    @Override
    @Deprecated
    void delete(Book entity); // disabled
}
