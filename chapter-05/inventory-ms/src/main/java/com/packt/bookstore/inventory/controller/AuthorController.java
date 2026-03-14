package com.packt.bookstore.inventory.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.bookstore.inventory.dto.AuthorRequest;
import com.packt.bookstore.inventory.dto.AuthorResponse;
import com.packt.bookstore.inventory.service.AuthorService;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        List<AuthorResponse> authors = authorService.findAll();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<AuthorResponse>> getAuthorsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AuthorResponse> paged = authorService.findAllPaginated(page, size);
        return ResponseEntity.ok(paged);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        AuthorResponse author = authorService.findById(id);
        return ResponseEntity.ok(author);
    }

    @GetMapping("/by-name")
    public ResponseEntity<AuthorResponse> getAuthorByName(@RequestParam String name) {
        AuthorResponse author = authorService.findByName(name);
        return ResponseEntity.ok(author);
    }

    @GetMapping("/by-name-ignore-case")
    public ResponseEntity<AuthorResponse> getAuthorByNameIgnoreCase(@RequestParam String name) {
        AuthorResponse author = authorService.findByNameIgnoreCase(name);
        return ResponseEntity.ok(author);
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@RequestBody AuthorRequest request) {
        AuthorResponse created = authorService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(@PathVariable Long id, @RequestBody AuthorRequest request) {
        AuthorResponse updated = authorService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}