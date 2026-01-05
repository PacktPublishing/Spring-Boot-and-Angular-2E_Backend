package com.packt.bookstore.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.packt.bookstore.inventory.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
        // Override the default findAll to always fetch authors
    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<Book> findAll(Pageable pageable);
    
    // Override findById to fetch author
    @Override
    @EntityGraph(attributePaths = {"author"})
    Optional<Book> findById(Long id);
    
    
     // Find books by title containing (case insensitive) with author loaded
    @EntityGraph(attributePaths = {"author"})
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    
    // Find books by title containing with pagination and author loaded
    @EntityGraph(attributePaths = {"author"})
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    

    @EntityGraph(attributePaths = {"author"})
    Optional<Book> findByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCase(String isbn);

    List<Book> findByAuthor_Name(String authorName);

    List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    // Custom JPQL Query
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND b.price > :minPrice")
    List<Book> searchBooks(@Param("keyword") String keyword, @Param("minPrice") BigDecimal minPrice);

    // Native SQL Query
    @Query(value = "SELECT * FROM books WHERE title ILIKE '%' || :keyword || '%'", nativeQuery = true)
    List<Book> nativeSearch(@Param("keyword") String keyword);

    // Fetching associated entities
    @Query("SELECT b FROM Book b JOIN FETCH b.author")
    List<Book> findAllWithAuthors();

    // Pagination support
    Page<Book> findByAuthor_Name(String name, Pageable pageable);
}
