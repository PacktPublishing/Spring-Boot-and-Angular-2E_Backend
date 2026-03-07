package com.packt.bookstore.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Paginated query with eager loading of books
    @EntityGraph(attributePaths = {"books"})
    @Query("SELECT a FROM Author a")
    Page<Author> findAllWithBooksPaginated(Pageable pageable);
    
    // Find author by name (case-insensitive) with eager loading - returns list to handle duplicates
    @EntityGraph(attributePaths = {"books"})
    @Query("SELECT a FROM Author a WHERE LOWER(a.name) = LOWER(:name)")
    List<Author> findByNameIgnoreCaseWithBooks(@Param("name") String name);
    
    // Find author by name (case-insensitive) - returns list to handle duplicates
    // Callers should use .stream().findFirst() to handle multiple matches
    List<Author> findByNameIgnoreCase(String name);
}
