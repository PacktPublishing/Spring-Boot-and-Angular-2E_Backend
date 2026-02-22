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
import com.packt.bookstore.inventory.event.BookEvent;
import com.packt.bookstore.inventory.event.NewBookEventData;
import com.packt.bookstore.inventory.event.PriceChangeEventData;
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
    private final NotificationService notificationService;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, 
                      BookMapper bookMapper, NotificationService notificationService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
        this.notificationService = notificationService;
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
        
        // Emit NEW_BOOK event for SSE subscribers
        publishNewBookEvent(saved);
        
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

        // Check if price is changing
        BigDecimal oldPrice = existing.getPrice();
        boolean priceChanged = req.price() != null && req.price().compareTo(oldPrice) != 0;

        Author author = null;
        if (req.authorName() != null)
            author = resolveAuthor(req.authorName());
        bookMapper.patch(existing, req, author);

        var saved = trySave(existing);
        
        // Emit PRICE_CHANGE event if price was updated
        if (priceChanged) {
            publishPriceChangeEvent(saved, oldPrice, saved.getPrice());
        }
        
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
                .stream()
                .findFirst() // Handle multiple matches by taking the first one
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
        if (req.authorName() == null || req.authorName().isBlank()) {
            throw new DomainRuleViolationException("Author name is required");
        }
        if (req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // Optional: proactive uniqueness for clearer error than a raw DB violation
        if (req.isbn() != null && bookRepository.existsByIsbnIgnoreCase(req.isbn())) {
            throw new DomainRuleViolationException("ISBN must be unique");
        }
    }

    private void validateSemanticsForReplace(BookRequest req) {
        if (req.authorName() == null || req.authorName().isBlank()) {
            throw new DomainRuleViolationException("Author name is required");
        }
        if (req.price().compareTo(BigDecimal.ZERO) < 0) {

            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // If needed, pre-check ISBN uniqueness here (same idea as create)
    }

    private void validateSemanticsForPatch(BookRequest req, Book existing) {
        if (req.price() != null && req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainRuleViolationException("Price cannot be negative");
        }
        // Only check uniqueness if ISBN is changing
        if (req.isbn() != null && !req.isbn().equalsIgnoreCase(existing.getIsbn())) {
            if (bookRepository.existsByIsbnIgnoreCase(req.isbn())) {
                throw new DomainRuleViolationException("ISBN must be unique");
            }
        }
    }

    /* ----------------- Event Publishing ----------------- */

    /**
     * Publishes a NEW_BOOK event to SSE subscribers
     */
    private void publishNewBookEvent(Book book) {
        try {
            NewBookEventData eventData = NewBookEventData.builder()
                    .authorName(book.getAuthor() != null ? book.getAuthor().getName() : null)
                    .genre(book.getGenre())
                    .price(book.getPrice())
                    .quantity(book.getQuantity())
                    .published(book.getPublished())
                    .description(book.getDescription())
                    .pageCount(book.getPageCount())
                    .coverImageUrl(book.getCoverImageUrl())
                    .build();

            BookEvent event = BookEvent.builder()
                    .eventType(BookEvent.EventType.NEW_BOOK)
                    .bookId(book.getId())
                    .bookTitle(book.getTitle())
                    .isbn(book.getIsbn())
                    .eventData(eventData)
                    .build();

            notificationService.publishEvent(event);
            log.info("Published NEW_BOOK event for book: {} (ID: {})", book.getTitle(), book.getId());
        } catch (Exception e) {
            log.error("Failed to publish NEW_BOOK event for book ID: {}", book.getId(), e);
            // Don't fail the transaction if event publishing fails
        }
    }

    /**
     * Publishes a PRICE_CHANGE event to SSE subscribers
     */
    private void publishPriceChangeEvent(Book book, BigDecimal oldPrice, BigDecimal newPrice) {
        try {
            BigDecimal priceChange = newPrice.subtract(oldPrice);
            double percentageChange = oldPrice.compareTo(BigDecimal.ZERO) > 0 
                    ? priceChange.divide(oldPrice, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).doubleValue()
                    : 0.0;

            PriceChangeEventData eventData = PriceChangeEventData.builder()
                    .oldPrice(oldPrice)
                    .newPrice(newPrice)
                    .priceChange(priceChange)
                    .percentageChange(percentageChange)
                    .build();

            BookEvent event = BookEvent.builder()
                    .eventType(BookEvent.EventType.PRICE_CHANGE)
                    .bookId(book.getId())
                    .bookTitle(book.getTitle())
                    .isbn(book.getIsbn())
                    .eventData(eventData)
                    .build();

            notificationService.publishEvent(event);
            log.info("Published PRICE_CHANGE event for book: {} (ID: {}) - Old: {}, New: {}", 
                    book.getTitle(), book.getId(), oldPrice, newPrice);
        } catch (Exception e) {
            log.error("Failed to publish PRICE_CHANGE event for book ID: {}", book.getId(), e);
            // Don't fail the transaction if event publishing fails
        }
    }

}
