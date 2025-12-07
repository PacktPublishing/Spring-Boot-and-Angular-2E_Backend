package com.packt.bookstore.inventory.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.entity.Book;
import com.packt.bookstore.inventory.exception.DomainRuleViolationException;
import com.packt.bookstore.inventory.exception.ResourceNotFoundException;
import com.packt.bookstore.inventory.mapper.BookMapper;
import com.packt.bookstore.inventory.repository.AuthorRepository;
import com.packt.bookstore.inventory.repository.BookRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookService implements IBookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BookResponse> findAll(int page, int size, String sortSpec) {
        Sort sortBy = parseSort(sortSpec);
        PageRequest pageRequest = PageRequest.of(page, size, sortBy);

        // This will now fetch books WITH authors due to @EntityGraph
        Page<Book> books = bookRepository.findAll(pageRequest);

        return books.getContent()
                .stream()
                .map(bookMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponse findOne(Long id) {
        log.info("Fetching book with id {}", id);
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));
        log.debug("Found book: {}", book.getTitle());
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse create(BookRequest req) {
        validateSemanticsForCreate(req); // 422 on rule violation
        var author = resolveAuthor(req.authorName());
        var toSave = bookMapper.toEntity(req, author);
        var saved = trySave(toSave); // may raise DataIntegrityViolationException → 409
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse replace(Long id, BookRequest req) {
        validateSemanticsForReplace(req); // 422 on rule violation
        var existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));
        var author = resolveAuthor(req.authorName());
        bookMapper.overwrite(existing, req, author);
        var saved = trySave(existing);
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse patch(Long id, BookRequest req) {
        var existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));

        validateSemanticsForPatch(req, existing); // 422 only on provided fields

        Author author = null;
        if (req.authorName() != null)
            author = resolveAuthor(req.authorName());
        bookMapper.patch(existing, req, author);

        var saved = trySave(existing);
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id))
            throw new ResourceNotFoundException("Book " + id + " not found");
        bookRepository.deleteById(id);
    }

    /* ----------------- helpers ----------------- */

    private Sort parseSort(String sortSpec) {
        if (sortSpec == null || sortSpec.isBlank())
            return Sort.by("title").ascending();
        var parts = sortSpec.split(",", 2);
        var field = parts[0].trim();
        var dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        return "desc".equals(dir) ? Sort.by(field).descending() : Sort.by(field).ascending();
    }

    private Author resolveAuthor(String name) {
        return authorRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> authorRepository.save(Author.builder().name(name).build()));
    }

    private Book trySave(Book entity) {
        try {
            return bookRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // Bubble up; GlobalExceptionHandler maps to 409 Conflict
            throw e;
        }
    }

    /* -------- semantic validations (422) -------- */

    private void validateSemanticsForCreate(BookRequest req) {
        if (req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // Optional: proactive uniqueness for clearer error than a raw DB violation
        if (req.isbn() != null && bookRepository.existsByIsbnIgnoreCase(req.isbn())) {
            throw new DomainRuleViolationException("ISBN must be unique");
        }
    }

    private void validateSemanticsForReplace(BookRequest req) {
        if (req.price().compareTo(BigDecimal.ZERO) < 0) {

            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // If needed, pre-check ISBN uniqueness here (same idea as create)
    }

    private void validateSemanticsForPatch(BookRequest req, Book existing) {
        if (req.price().compareTo(BigDecimal.ZERO) < 0) {

            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // Only check uniqueness if ISBN is changing
        if (req.isbn() != null && !req.isbn().equalsIgnoreCase(existing.getIsbn())) {
            if (bookRepository.existsByIsbnIgnoreCase(req.isbn())) {
                throw new DomainRuleViolationException("ISBN must be unique");
            }
        }
    }

}
