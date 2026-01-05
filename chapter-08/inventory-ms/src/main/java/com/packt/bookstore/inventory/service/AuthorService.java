package com.packt.bookstore.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.bookstore.inventory.dto.AuthorRequest;
import com.packt.bookstore.inventory.dto.AuthorResponse;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.mapper.AuthorMapper;
import com.packt.bookstore.inventory.repository.AuthorRepository;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll()
                .stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    public Page<AuthorResponse> findAllPaginated(int page, int size) {
        return authorRepository.findAll(PageRequest.of(page, size))
                .map(authorMapper::toResponse);
    }
@Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        return authorMapper.toResponse(author);
    }

    public AuthorResponse findByName(String name) {
        Author author = authorRepository.findByName(name);
        if (author == null)
            throw new RuntimeException("Author not found");
        return authorMapper.toResponse(author);
    }

    public AuthorResponse findByNameIgnoreCase(String name) {
        Optional<Author> authorOpt = authorRepository.findByNameIgnoreCase(name);
        Author author = authorOpt.orElseThrow(() -> new RuntimeException("Author not found"));
        return authorMapper.toResponse(author);
    }

    public AuthorResponse create(AuthorRequest request) {
        Author author = authorMapper.toEntity(request);
        Author saved = authorRepository.save(author);
        return authorMapper.toResponse(saved);
    }

    public AuthorResponse update(Long id, AuthorRequest request) {
    Author existing = authorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Author not found"));
    
    // Create new author with updated fields
    Author updated = Author.builder()
            .id(existing.getId())
            .name(request.name())
            .nationality(request.nationality())
            .books(existing.getBooks())
            .build();
    
    Author saved = authorRepository.save(updated);
    return authorMapper.toResponse(saved);
}

    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new RuntimeException("Author not found");
        }
        authorRepository.deleteById(id);
    }
}