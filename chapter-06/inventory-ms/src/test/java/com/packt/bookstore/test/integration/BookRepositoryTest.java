package com.packt.bookstore.test.integration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.packt.bookstore.inventory.InventoryMsApplication;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;
import com.packt.bookstore.inventory.repository.AuthorRepository;
import com.packt.bookstore.inventory.repository.BookRepository;

@SpringBootTest(classes = InventoryMsApplication.class)
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;

    private Author author;
    private Book book;

   @BeforeEach
void setUp() {
    // Clear previous data
    bookRepository.deleteAll();
    authorRepository.deleteAll();
    
    // Create and save author first
    author = Author.builder()
            .name("Ahmad Gohar")
            .nationality("Egyptian")
            .build();
    author = authorRepository.save(author);  // Save and get the ID
    
    // Now create and save book with the author
    book = Book.builder()
            .title("Spring Boot and Angular 2E")
            .isbn("1234567890")
            .author(author)
            .price(BigDecimal.valueOf(29.99))
            .quantity(10)
            .build();
    book = bookRepository.save(book);  // Save the book and get the ID
    
    // Verify the book was actually saved
    assert bookRepository.count() > 0 : "Book was not saved correctly";
}

    @Test
void testFindAll() {
    System.out.println("Book count: " + bookRepository.count());
    var books = bookRepository.findAll();
    System.out.println("Books found: " + books.size());
    assertThat(books).hasSize(1);
}
    
@Test
void testFindByTitle() {
    // Method returns a List<Book> not a single Book
    var foundBooks = bookRepository.findByTitleContainingIgnoreCase("Spring Boot and Angular 2E");
    assertThat(foundBooks).isNotEmpty();
    
    // Get the first book from the list
    var foundBook = foundBooks.get(0);
    assertThat(foundBook).isNotNull();
    assertThat(foundBook.getIsbn()).isEqualTo("1234567890");
}
}
