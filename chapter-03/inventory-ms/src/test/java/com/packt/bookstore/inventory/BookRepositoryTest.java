package com.packt.bookstore.inventory;

import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;
import com.packt.bookstore.inventory.repository.AuthorRepository;
import com.packt.bookstore.inventory.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author author;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        author = new Author();
        author.setName("Ahmad Gohar");
        author.setNationality("Egyptian");
        author = authorRepository.save(author);

        Book book = new Book();
        book.setTitle("Spring Boot and Angular - 2nd Edition");
        book.setIsbn("1234567890");
        book.setPrice(new BigDecimal("19.99"));
        book.setQuantity(10);
        book.setAuthor(author);
        bookRepository.save(book);
    }

    @Test
    void testFindAll() {
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Spring Boot and Angular - 2nd Edition");
    }

    @Test
    void testBookAuthorRelationship() {
        List<Book> books = bookRepository.findAll();
        assertThat(books.get(0).getAuthor().getName()).isEqualTo("Ahmad Gohar");
    }

    @Test
    void testFindByTitleContainingIgnoreCase() {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase("spring");
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Spring Boot and Angular - 2nd Edition");
    }

    @Test
    void testFindByAuthorName() {
        List<Book> books = bookRepository.findByAuthor_Name("Ahmad Gohar");
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor().getName()).isEqualTo("Ahmad Gohar");
    }

    @Test
    void testFindByPriceBetween() {
        List<Book> books = bookRepository.findByPriceBetween(new BigDecimal("10.00"), new BigDecimal("20.00"));
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getPrice()).isEqualTo(new BigDecimal("19.99"));
    }

    @Test
    void testSearchBooks() {
        List<Book> books = bookRepository.searchBooks("Spring", new BigDecimal("10.00"));
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Spring Boot and Angular - 2nd Edition");
    }

    @Test
    void testFindAllWithAuthors() {
        List<Book> books = bookRepository.findAllWithAuthors();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor().getName()).isEqualTo("Ahmad Gohar");
    }

    @Test
    void testNativeSearch() {
        List<Book> books = bookRepository.nativeSearch("Spring");
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Spring Boot and Angular - 2nd Edition");
    }

    @Test
    void testFindByAuthorNameWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = bookRepository.findByAuthor_Name("Ahmad Gohar", pageable);
        assertThat(bookPage.getContent()).hasSize(1);
        assertThat(bookPage.getContent().get(0).getAuthor().getName()).isEqualTo("Ahmad Gohar");
        assertThat(bookPage.getTotalElements()).isEqualTo(1);
        assertThat(bookPage.getTotalPages()).isEqualTo(1);
    }

    private Specification<Book> hasTitleLike(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<Book> priceGreaterThan(BigDecimal price) {
        return (root, query, cb) -> cb.greaterThan(root.get("price"), price);
    }

    @Test
    void testBookSpecificationsWithPrice() {
        Specification<Book> spec = hasTitleLike("spring")
                .and(priceGreaterThan(new BigDecimal("10")));

        List<Book> results = bookRepository.findAll(spec);
        assertThat(results).hasSize(1); // One book with price > 10
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot and Angular - 2nd Edition");
    }

}
