# Chapter 05 — Building Application Services and APIs with Spring Boot

This chapter focuses on building the **service layer** and exposing your business logic through **RESTful APIs** using Spring Boot. You'll learn how to implement dependency injection, design robust service layers, create REST controllers, handle validation and errors, version your APIs, and thoroughly test your implementation.

---

## Table of Contents

1. [Chapter Overview](#chapter-overview)
2. [Spring Boot Dependency Injection & Component Scanning](#spring-boot-dependency-injection--component-scanning)
3. [Service Layer Design](#service-layer-design)
4. [Business Logic & Exception-Aware Workflows](#business-logic--exception-aware-workflows)
5. [RESTful API Design with Spring Boot](#restful-api-design-with-spring-boot)
6. [DTOs, Validation & Error Handling](#dtos-validation--error-handling)
7. [API Versioning Strategies](#api-versioning-strategies)
8. [Testing the Service & Controller Layers](#testing-the-service--controller-layers)
9. [Installation & Setup Steps](#installation--setup-steps)
10. [Resources & References](#resources--references)

---

## Chapter Overview

This chapter brings your microservices to life by implementing the business logic and API layers. You'll learn:



## ✅ Before You Run This Chapter

Please confirm the required runtime dependencies before running this chapter:

- Confirm the database is started (PostgreSQL and MongoDB for this chapter).
- Confirm any infrastructure dependencies are running (for example Docker services, if used).
- Confirm any dependencies from previous chapters are running as needed for your flow.

### Check if Databases Are Running

#### PostgreSQL
```bash
docker ps | grep bookstore-postgres
```
#### MongoDB
```bash
docker ps | grep bookstore-mongo
```

### Start PostgreSQL Container (Inventory DB)
```bash
docker run -d \
    --name bookstore-postgres \
    -e POSTGRES_USER=bookstore \
    -e POSTGRES_PASSWORD=bookstore123 \
    -e POSTGRES_DB=inventory \
    -p 5432:5432 \
    postgres:17
```

### Start MongoDB Container (User DB)
```bash
docker run -d \
    --name bookstore-mongo \
    -e MONGO_INITDB_ROOT_USERNAME=bookstore \
    -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
    -e MONGO_INITDB_DATABASE=userDB \
    -p 27017:27017 \
    mongo:8
```

## 📦 Chapter Source Code Availability

The final source code for this chapter is already uploaded in this directory.

Use this folder as the reference implementation for the completed chapter state.

## Spring Boot Dependency Injection & Component Scanning

Spring Boot uses stereotype annotations to define components and manage dependencies automatically through component scanning.

### Core Spring Stereotypes

| Annotation | Purpose | Layer |
|------------|---------|-------|
| **@Component** | Generic Spring bean | Any |
| **@Service** | Business logic | Service layer |
| **@Repository** | Data access | Persistence layer |
| **@RestController** | REST API endpoints | Controller layer |
| **@Configuration** | Configuration class | Configuration |

### Component Scanning

Spring Boot automatically scans for components in the main application package and sub-packages:

```java
@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
```

### Example: BookMapper Component

```java
@Component
public class BookMapper {
    
    public Book toEntity(BookRequest req, Author author) {
        Book book = new Book();
        book.setTitle(req.title());
        book.setIsbn(req.isbn());
        book.setAuthor(author);
        book.setPrice(req.price());
        book.setQuantity(req.quantity());
        return book;
    }
    
    public BookResponse toResponse(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getAuthor().getName(),
            book.getPrice(),
            book.getQuantity()
        );
    }
}
```

### Dependency Injection Patterns

**Constructor Injection (Recommended)**

```java
@Service
public class BookService {
    
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper mapper;
    
    // Constructor injection - mandatory dependencies
    public BookService(BookRepository bookRepository, 
                      AuthorRepository authorRepository,
                      BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.mapper = mapper;
    }
}
```

**Field Injection (Not Recommended)**

```java
@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository; // Harder to test
}
```

---

## Service Layer Design

The service layer orchestrates business logic, enforces domain rules, and coordinates repository operations.

### Service Interface

```java
public interface IBookService {
    List<BookResponse> findAll(int page, int size, String sort);
    BookResponse findOne(Long id);
    BookResponse create(BookRequest req);
    BookResponse replace(Long id, BookRequest req);
    BookResponse patch(Long id, BookRequest req);
    void delete(Long id);
}
```

### Service Implementation

```java
@Service
@Transactional(readOnly = true)
public class BookService implements IBookService {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final BookMapper mapper;

    public BookService(BookRepository books, 
                      AuthorRepository authors, 
                      BookMapper mapper) {
        this.books = books;
        this.authors = authors;
        this.mapper = mapper;
    }

    // Read operations
    @Override
    public List<BookResponse> findAll(int page, int size, String sortSpec) {
        Sort sort = parseSort(sortSpec);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> result = books.findAll(pageable);
        return result.map(mapper::toResponse).getContent();
    }

    @Override
    public BookResponse findOne(Long id) {
        Book book = books.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        return mapper.toResponse(book);
    }

    // Write operations
    @Override
    @Transactional
    public BookResponse create(BookRequest req) {
        validateSemanticsForCreate(req);
        Author author = resolveAuthor(req.authorName());
        Book book = mapper.toEntity(req, author);
        Book saved = books.save(book);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse replace(Long id, BookRequest req) {
        Book existing = books.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        validateSemanticsForUpdate(req, existing);
        Author author = resolveAuthor(req.authorName());
        
        existing.setTitle(req.title());
        existing.setIsbn(req.isbn());
        existing.setAuthor(author);
        existing.setPrice(req.price());
        existing.setQuantity(req.quantity());
        
        Book saved = books.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse patch(Long id, BookRequest req) {
        Book existing = books.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        if (req.title() != null) {
            existing.setTitle(req.title());
        }
        if (req.price() != null) {
            existing.setPrice(req.price());
        }
        if (req.quantity() != null) {
            existing.setQuantity(req.quantity());
        }
        
        Book saved = books.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!books.existsById(id)) {
            throw new ResourceNotFoundException("Book", id);
        }
        books.deleteById(id);
    }

    // Helper methods
    private Author resolveAuthor(String name) {
        return authors.findByName(name)
            .orElseGet(() -> {
                Author newAuthor = new Author();
                newAuthor.setName(name);
                return authors.save(newAuthor);
            });
    }

    private Sort parseSort(String sortSpec) {
        if (sortSpec == null || sortSpec.isEmpty()) {
            return Sort.by("id").ascending();
        }
        
        String[] parts = sortSpec.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && 
            parts[1].equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        return Sort.by(direction, property);
    }

    private void validateSemanticsForCreate(BookRequest req) {
        if (books.findByIsbn(req.isbn()).isPresent()) {
            throw new DomainRuleViolationException(
                "Book with ISBN " + req.isbn() + " already exists");
        }
    }

    private void validateSemanticsForUpdate(BookRequest req, Book existing) {
        if (!existing.getIsbn().equals(req.isbn())) {
            books.findByIsbn(req.isbn()).ifPresent(book -> {
                throw new DomainRuleViolationException(
                    "ISBN " + req.isbn() + " is already used by another book");
            });
        }
    }
}
```

---

## Business Logic & Exception-Aware Workflows

### Custom Exception Classes

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s with id %d not found", resource, id));
    }
}
```

```java
public class DomainRuleViolationException extends RuntimeException {
    public DomainRuleViolationException(String message) {
        super(message);
    }
}
```

### Transaction Management

```java
@Service
@Transactional(readOnly = true) // Default for all methods
public class BookService {

    @Transactional // Override for write operations
    public BookResponse create(BookRequest req) {
        // Multiple repository operations in single transaction
        Author author = resolveAuthor(req.authorName());
        Book book = mapper.toEntity(req, author);
        return mapper.toResponse(books.save(book));
    }
}
```

---

## RESTful API Design with Spring Boot

### Application Configuration

```yaml
server:
  port: 8081
  servlet:
    context-path: /inventory

spring:
  application:
    name: inventory-service
```

### REST Controller

```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        
        List<BookResponse> books = bookService.findAll(page, size, sort);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.findOne(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest request) {
        
        BookResponse created = bookService.create(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> replaceBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        
        BookResponse updated = bookService.replace(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> patchBook(
            @PathVariable Long id,
            @RequestBody BookRequest request) {
        
        BookResponse updated = bookService.patch(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### HTTP Status Codes

| Status Code | Usage | Example |
|-------------|-------|---------|
| 200 OK | Successful GET, PUT, PATCH | Retrieving or updating a book |
| 201 Created | Successful POST | Creating a new book |
| 204 No Content | Successful DELETE | Deleting a book |
| 400 Bad Request | Validation failure | Invalid ISBN format |
| 404 Not Found | Resource not found | Book ID doesn't exist |
| 409 Conflict | Business rule violation | Duplicate ISBN |
| 500 Internal Error | Unexpected server error | Database connection failure |

---

## DTOs, Validation & Error Handling

### BookRequest DTO with Validation

```java
public record BookRequest(
    @NotBlank(message = "Title is required")
    String title,
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9-]{10,17}$", message = "Invalid ISBN format")
    String isbn,
    
    @NotBlank(message = "Author name is required")
    String authorName,
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or positive")
    BigDecimal price,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be zero or positive")
    Integer quantity
) {}
```

### BookResponse DTO

```java
public record BookResponse(
    Long id,
    String title,
    String isbn,
    String authorName,
    BigDecimal price,
    Integer quantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ApiError> handleDomainRuleViolation(
            DomainRuleViolationException ex,
            WebRequest request) {
        
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### ApiError Class

```java
@Data
@AllArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private Object details;
    private LocalDateTime timestamp;

    public ApiError(int status, String message, Object details) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
```

---

## API Versioning Strategies

### Comparison of Versioning Strategies

| Strategy | Implementation | Pros | Cons | Best For |
|---------|----------------|------|------|----------|
| **URI Versioning** | `/api/v1/books` | Simple, visible, easy to test | URL churn, routing complexity | Public APIs, mobile apps |
| **Header Versioning** | `X-API-Version: 1` | Clean URLs, flexible | Harder to debug, test | Internal APIs, B2B |
| **Content Negotiation** | `Accept: application/vnd.api+json;version=1` | RESTful, standard | Client complexity | Enterprise systems |
| **Query Parameter** | `/api/books?version=1` | Simple | Not RESTful | Legacy support |

### URI Versioning Example

```java
@RestController
@RequestMapping("/api/v1/books")
public class BookControllerV1 {
    // Version 1 implementation
}

@RestController
@RequestMapping("/api/v2/books")
public class BookControllerV2 {
    // Version 2 with new features
}
```

### Header Versioning Example

```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping(headers = "X-API-Version=1")
    public ResponseEntity<List<BookResponseV1>> getAllBooksV1() {
        // Version 1 logic
    }

    @GetMapping(headers = "X-API-Version=2")
    public ResponseEntity<List<BookResponseV2>> getAllBooksV2() {
        // Version 2 logic
    }
}
```

---

## Testing the Service & Controller Layers

### Service Layer Tests

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void findOne_shouldReturnBook_whenExists() {
        // Given
        Long id = 1L;
        Book book = new Book();
        book.setId(id);
        BookResponse response = new BookResponse(id, "Test", "123", "Author", 
                                                 BigDecimal.TEN, 5, null, null);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(mapper.toResponse(book)).thenReturn(response);

        // When
        BookResponse result = bookService.findOne(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        verify(bookRepository).findById(id);
    }

    @Test
    void findOne_shouldThrow_whenNotFound() {
        // Given
        Long id = 999L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                    () -> bookService.findOne(id));
    }

    @Test
    void create_shouldSaveBook_whenValid() {
        // Given
        BookRequest request = new BookRequest("Test Book", "1234567890", 
                                              "John Doe", BigDecimal.TEN, 5);
        Author author = new Author();
        author.setName("John Doe");
        Book book = new Book();
        BookResponse response = new BookResponse(1L, "Test Book", "1234567890",
                                                 "John Doe", BigDecimal.TEN, 5, null, null);

        when(authorRepository.findByName("John Doe")).thenReturn(Optional.of(author));
        when(bookRepository.findByIsbn(request.isbn())).thenReturn(Optional.empty());
        when(mapper.toEntity(request, author)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(mapper.toResponse(book)).thenReturn(response);

        // When
        BookResponse result = bookService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Book");
        verify(bookRepository).save(book);
    }

    @Test
    void create_shouldThrow_whenIsbnExists() {
        // Given
        BookRequest request = new BookRequest("Test Book", "1234567890",
                                              "John Doe", BigDecimal.TEN, 5);
        Book existing = new Book();
        when(bookRepository.findByIsbn(request.isbn())).thenReturn(Optional.of(existing));

        // When & Then
        assertThrows(DomainRuleViolationException.class,
                    () -> bookService.create(request));
    }
}
```

### Controller Layer Tests

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBooks_shouldReturn200() throws Exception {
        // Given
        List<BookResponse> books = List.of(
            new BookResponse(1L, "Book 1", "123", "Author 1", 
                           BigDecimal.TEN, 5, null, null)
        );
        when(bookService.findAll(0, 10, null)).thenReturn(books);

        // When & Then
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Book 1"));
    }

    @Test
    void getBookById_shouldReturn200_whenExists() throws Exception {
        // Given
        Long id = 1L;
        BookResponse response = new BookResponse(id, "Test Book", "123", "Author",
                                                 BigDecimal.TEN, 5, null, null);
        when(bookService.findOne(id)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/books/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void getBookById_shouldReturn404_whenNotFound() throws Exception {
        // Given
        Long id = 999L;
        when(bookService.findOne(id))
            .thenThrow(new ResourceNotFoundException("Book", id));

        // When & Then
        mockMvc.perform(get("/api/books/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void createBook_shouldReturn201_whenValid() throws Exception {
        // Given
        BookRequest request = new BookRequest("New Book", "1234567890",
                                              "Author", BigDecimal.TEN, 5);
        BookResponse response = new BookResponse(1L, "New Book", "1234567890",
                                                 "Author", BigDecimal.TEN, 5, null, null);
        when(bookService.create(any(BookRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    void createBook_shouldReturn400_whenInvalid() throws Exception {
        // Given
        BookRequest request = new BookRequest("", "", "", null, null);

        // When & Then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBook_shouldReturn204_whenExists() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(bookService).delete(id);

        // When & Then
        mockMvc.perform(delete("/api/books/{id}", id))
            .andExpect(status().isNoContent());
    }
}
```

---

## Installation & Setup Steps

### 1. Add Required Dependencies

Add to `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Starter Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. Configure Application Properties

spring:
```yaml
server:
    port: 8081
    servlet:
        context-path: /inventory

spring:
    application:
        name: inventory-service

    main:
        allow-bean-definition-overriding: true

    profiles:
        active: dev

    java:
        version: 25

logging:
    level:
        com.bookstore: DEBUG
        org.springframework.web: DEBUG
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

### 4. Test the API

```bash
# Get all books
curl http://localhost:8081/inventory/api/books

# Get book by ID
curl http://localhost:8081/inventory/api/books/1

# Create new book
curl -X POST http://localhost:8081/inventory/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot Guide",
    "isbn": "1234567890",
    "authorName": "John Doe",
    "price": 29.99,
    "quantity": 100
  }'

# Update book
curl -X PUT http://localhost:8081/inventory/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "isbn": "1234567890",
    "authorName": "John Doe",
    "price": 34.99,
    "quantity": 90
  }'

# Delete book
curl -X DELETE http://localhost:8081/inventory/api/books/1
```

---

## Resources & References

- **Spring Boot Documentation**: <https://spring.io/projects/spring-boot>
- **Spring Web MVC**: <https://docs.spring.io/spring-framework/reference/web/webmvc.html>
- **Bean Validation (JSR-380)**: <https://beanvalidation.org/>
- **Testing Spring Boot Applications**: <https://spring.io/guides/gs/testing-web/>
- **MockMvc Documentation**: <https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html>

---
