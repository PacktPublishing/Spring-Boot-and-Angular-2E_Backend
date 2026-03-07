package com.packt.bookstore.test.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.packt.bookstore.inventory.InventoryMsApplication;
import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.repository.AuthorRepository;

@SpringBootTest(classes = InventoryMsApplication.class)  // Explicitly specify the configuration class
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
        Author author = Author.builder()
                .name("Ahmad Gohar")
                .nationality("Egyptian")
                .build();
        authorRepository.save(author);
    }

    @Test
    void testFindAll() {
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(1);
    }

    @Test
    void testFindByName() {
        Author author = authorRepository.findByName("Ahmad Gohar");
        assertThat(author).isNotNull();
        assertThat(author.getNationality()).isEqualTo("Egyptian");
    }
}