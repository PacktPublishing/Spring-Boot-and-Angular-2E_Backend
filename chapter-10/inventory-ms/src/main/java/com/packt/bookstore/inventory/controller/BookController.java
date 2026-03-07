package com.packt.bookstore.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.bookstore.inventory.dto.ApiError;
import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;
import com.packt.bookstore.inventory.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Book Inventory", description = "Operations related to book catalog and inventory")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
        summary = "Retrieve all books with pagination and sorting",
        description = """
            Fetches a paginated list of books from the inventory system. This endpoint supports:
            - Pagination through 'page' and 'size' parameters
            - Sorting by any book field using the 'sort' parameter
            - Returns comprehensive book information including author details
            
            Example usage:
            - GET /api/books?page=0&size=20&sort=title,asc
            - GET /api/books?page=1&size=5&sort=published,desc
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved books list",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BookResponse.class)),
                examples = @ExampleObject(
                    name = "Books List Example",
                    value = """
                        [
                          {
                            "id": 1,
                            "title": "Spring Boot in Action",
                            "isbn": "978-1617292545",
                            "price": 39.99,
                            "quantity": 15,
                            "author": {
                              "id": 1,
                              "name": "Craig Walls"
                            },
                            "genre": "Technology",
                            "published": "2015-12-27",
                            "description": "A comprehensive guide to Spring Boot",
                            "pageCount": 472,
                            "coverImageUrl": "https://example.com/cover.jpg"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters (e.g., negative page number, invalid sort field)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error occurred while processing the request",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @Parameter(
                name = "page",
                description = "Page number for pagination (0-based indexing)",
                example = "0",
                schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                name = "size",
                description = "Number of books per page (maximum 100 items per page)",
                example = "10",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(
                name = "sort",
                description = """
                    Sorting criteria in the format: property(,asc|desc). 
                    Default sort order is ascending. Multiple sort criteria are supported.
                    
                    Available sort fields:
                    - id: Book ID
                    - title: Book title
                    - isbn: ISBN number
                    - price: Book price
                    - quantity: Available quantity
                    - genre: Book genre
                    - published: Publication date
                    - pageCount: Number of pages
                    """,
                example = "title,asc",
                schema = @Schema(type = "string")
            )
            @RequestParam(required = false) String sort
    ) {
        log.info("Fetching all books - page: {}, size: {}, sort: {}", page, size, sort);
        List<BookResponse> books = bookService.findAll(page, size, sort);
        log.info("Successfully retrieved {} books", books.size());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        log.info("Fetching book with id {}", id);
        BookResponse book = bookService.findOne(id);
        log.info("Successfully retrieved book: {}", book.title());
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> replaceBook(@PathVariable Long id, @RequestBody BookRequest request) {
        BookResponse updated = bookService.replace(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
        BookResponse patched = bookService.patch(id, request);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
         return ResponseEntity.noContent().build();
    }
}