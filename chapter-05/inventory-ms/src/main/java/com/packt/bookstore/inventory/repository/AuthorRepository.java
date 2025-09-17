package com.packt.bookstore.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.packt.bookstore.inventory.entity.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Author findByName(String name);

     // Fetch authors with books in a single query
    @EntityGraph(attributePaths = {"books"})
    @Query("SELECT a FROM Author a")
    List<Author> findAllWithBooks();
    
    @EntityGraph(attributePaths = {"books"})
    @Query("SELECT a FROM Author a WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);
    
    // Regular methods without books (when you don't need them)
    Optional<Author> findByNameIgnoreCase(String name);
}
