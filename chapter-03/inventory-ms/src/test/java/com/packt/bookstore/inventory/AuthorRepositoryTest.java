package com.packt.bookstore.inventory;

import com.packt.bookstore.inventory.entity.Author;
import com.packt.bookstore.inventory.repository.AuthorRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
        Author author = new Author();
        author.setName("Ahmad Gohar");
        author.setNationality("Egyptian");
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