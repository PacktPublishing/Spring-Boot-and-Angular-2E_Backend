package com.packt.bookstore.test.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;
import com.packt.bookstore.inventory.exception.DomainRuleViolationException;
import com.packt.bookstore.inventory.exception.ResourceNotFoundException;
import com.packt.bookstore.inventory.mapper.BookMapper;
import com.packt.bookstore.inventory.repository.AuthorRepository;
import com.packt.bookstore.inventory.repository.BookRepository;
import com.packt.bookstore.inventory.service.BookService;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private Author author;
    private Book book;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        author = Author.builder().id(1L).name("Ahmad Gohar").build();
        book = Book.builder().id(1L).title("Spring Boot and Angular 2E").isbn("1234567890").author(author).price(BigDecimal.TEN).build();
        bookRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.TEN,null,null,null,null,null);
        bookResponse = new BookResponse(
                1L,
                "Spring Boot and Angular 2E",
                "1234567890",
                "Ahmad Gohar",
                BigDecimal.TEN,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void findAll_shouldReturnBookResponses() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.findAll(0, 10, "title,asc");
        assertEquals(1, result.size());
        assertEquals(bookResponse, result.get(0));
    }

    @Test
    void findOne_shouldReturnBookResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.findOne(1L);
        assertEquals(bookResponse, result);
    }

    @Test
    void findOne_shouldThrowResourceNotFoundException() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.findOne(2L));
    }

    @Test
    void create_shouldSaveAndReturnBookResponse() {
        when(authorRepository.findByNameIgnoreCase("Ahmad Gohar")).thenReturn(Optional.of(author));
        when(bookMapper.toEntity(bookRequest, author)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.create(bookRequest);
        assertEquals(bookResponse, result);
    }

    @Test
    void create_shouldThrowDomainRuleViolationExceptionForNegativePrice() {
        BookRequest badRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.valueOf(-1), null, null, null, null, null);
        DomainRuleViolationException exception = assertThrows(DomainRuleViolationException.class, () -> bookService.create(badRequest));
    }

    @Test
    void create_shouldThrowDomainRuleViolationExceptionForDuplicateIsbn() {
        when(bookRepository.existsByIsbnIgnoreCase("1234567890")).thenReturn(true);
        assertThrows(DomainRuleViolationException.class, () -> bookService.create(bookRequest));
    }

    @Test
    void replace_shouldUpdateAndReturnBookResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameIgnoreCase("Ahmad Gohar")).thenReturn(Optional.of(author));
        doNothing().when(bookMapper).overwrite(book, bookRequest, author);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.replace(1L, bookRequest);
        assertEquals(bookResponse, result);
    }

    @Test
    void replace_shouldThrowResourceNotFoundException() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.replace(2L, bookRequest));
    }

    @Test
    void patch_shouldThrowResourceNotFoundException() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.patch(2L, bookRequest));
    }

    @Test
    void patch_shouldThrowDomainRuleViolationExceptionForNegativePrice() {
        BookRequest badRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.valueOf(-1), null, null, null, null, null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        assertThrows(DomainRuleViolationException.class, () -> bookService.patch(1L, badRequest));
    }

    @Test
    void patch_shouldThrowDomainRuleViolationExceptionForDuplicateIsbn() {
        BookRequest newIsbnRequest = new BookRequest("Spring Boot and Angular 2E", "NEWISBN", "Ahmad Gohar", BigDecimal.TEN, null, null, null, null, null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnIgnoreCase("NEWISBN")).thenReturn(true);

        assertThrows(DomainRuleViolationException.class, () -> bookService.patch(1L, newIsbnRequest));
    }

    @Test
    void delete_shouldRemoveBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        assertDoesNotThrow(() -> bookService.delete(1L));
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(bookRepository.existsById(2L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bookService.delete(2L));
    }

    @Test
    void resolveAuthor_shouldReturnExistingAuthor() throws Exception {
        when(authorRepository.findByNameIgnoreCase("Ahmad Gohar")).thenReturn(Optional.of(author));

        var method = BookService.class.getDeclaredMethod("resolveAuthor", String.class);
        method.setAccessible(true);
        Author result = (Author) method.invoke(bookService, "Ahmad Gohar");

        assertEquals(author, result);
    }

    @Test
    void resolveAuthor_shouldCreateAndReturnNewAuthor() throws Exception {
        when(authorRepository.findByNameIgnoreCase("Ahmad Gohar")).thenReturn(Optional.empty());
        Author newAuthor = Author.builder().name("Ahmad Gohar").build();
        when(authorRepository.save(any(Author.class))).thenReturn(newAuthor);

        var method = BookService.class.getDeclaredMethod("resolveAuthor", String.class);
        method.setAccessible(true);
        Author result = (Author) method.invoke(bookService, "Ahmad Gohar");
        
        assertEquals(newAuthor, result);
    }

    @Test
    void trySave_shouldReturnSavedBook() throws Exception {
        when(bookRepository.save(book)).thenReturn(book);
        
        var method = BookService.class.getDeclaredMethod("trySave", Book.class);
        method.setAccessible(true);
        Book result = (Book) method.invoke(bookService, book);
        
        assertEquals(book, result);
    }

    @Test
    void trySave_shouldThrowDataIntegrityViolationException() throws Exception {
        when(bookRepository.save(book)).thenThrow(new DataIntegrityViolationException("Duplicate"));
        
        var method = BookService.class.getDeclaredMethod("trySave", Book.class);
        method.setAccessible(true);
        
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> method.invoke(bookService, book));
    }

    @Test
    void parseSort_shouldReturnDefaultSort() throws Exception {
        var method = BookService.class.getDeclaredMethod("parseSort", String.class);
        method.setAccessible(true);
        
        Sort sort = (Sort) method.invoke(bookService, (String)null);
        assertEquals(Sort.by("title").ascending(), sort);

        sort = (Sort) method.invoke(bookService, "");
        assertEquals(Sort.by("title").ascending(), sort);
    }

    @Test
    void parseSort_shouldReturnAscendingSort() throws Exception {
        var method = BookService.class.getDeclaredMethod("parseSort", String.class);
        method.setAccessible(true);
        
        Sort sort = (Sort) method.invoke(bookService, "title,asc");
        assertEquals(Sort.by("title").ascending(), sort);
    }

    @Test
    void parseSort_shouldReturnDescendingSort() throws Exception {
        var method = BookService.class.getDeclaredMethod("parseSort", String.class);
        method.setAccessible(true);
        
        Sort sort = (Sort) method.invoke(bookService, "title,desc");
        assertEquals(Sort.by("title").descending(), sort);
    }

    @Test
    void validateSemanticsForCreate_shouldThrowForNegativePrice() {
        BookRequest badRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.valueOf(-1), null, null, null, null, null);
        assertThrows(DomainRuleViolationException.class, () -> {
            try {
                var method = BookService.class.getDeclaredMethod("validateSemanticsForCreate", BookRequest.class);
                method.setAccessible(true);
                method.invoke(bookService, badRequest);
            } catch (Exception e) {
                if (e.getCause() instanceof DomainRuleViolationException) {
                    throw (DomainRuleViolationException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void validateSemanticsForReplace_shouldThrowForNegativePrice() {
        BookRequest badRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.valueOf(-1), null, null, null, null, null);
        assertThrows(DomainRuleViolationException.class, () -> {
            try {
                var method = BookService.class.getDeclaredMethod("validateSemanticsForReplace", BookRequest.class);
                method.setAccessible(true);
                method.invoke(bookService, badRequest);
            } catch (Exception e) {
                if (e.getCause() instanceof DomainRuleViolationException) {
                    throw (DomainRuleViolationException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void validateSemanticsForPatch_shouldThrowForNegativePrice() {
        BookRequest badRequest = new BookRequest("Spring Boot and Angular 2E", "1234567890", "Ahmad Gohar", BigDecimal.valueOf(-1), null, null, null, null, null);
        assertThrows(DomainRuleViolationException.class, () -> {
            try {
                var method = BookService.class.getDeclaredMethod("validateSemanticsForPatch", BookRequest.class, Book.class);
                method.setAccessible(true);
                method.invoke(bookService, badRequest, book);
            } catch (Exception e) {
                if (e.getCause() instanceof DomainRuleViolationException) {
                    throw (DomainRuleViolationException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
}