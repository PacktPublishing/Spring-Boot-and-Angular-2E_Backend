package com.packt.bookstore.test.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.packt.bookstore.inventory.controller.BookController;
import com.packt.bookstore.inventory.dto.BookRequest;
import com.packt.bookstore.inventory.dto.BookResponse;
import com.packt.bookstore.inventory.service.BookService;

public class BookControllerTest {
    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookResponse bookResponse;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
        // Create BookResponse using constructor with all required parameters
        bookResponse = new BookResponse(
            1L,
            "Spring Boot and Angular 2E",
            "1234567890",
            "Ahmad Gohar",
            new BigDecimal("44.99"),
            "Programming",
            LocalDate.of(2024, 1, 1),
            "a full stack guide to modern web development using  Java, Spring and Angular",
            10,
            "2023-09-06T12:00:00"
        );
      
    }

    @Test
    void getAllBooks_shouldReturnBooks() throws Exception {
        when(bookService.findAll(anyInt(), anyInt(), anyString()))
            .thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/books")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").value("Spring Boot and Angular 2E"))
            .andExpect(jsonPath("$[0].isbn").value("1234567890"))
            .andExpect(jsonPath("$[0].authorName").value("Ahmad Gohar"));
    }

  

    @Test
    void getBookById_shouldReturnBook() throws Exception {
        when(bookService.findOne(anyLong())).thenReturn(bookResponse);

        mockMvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Spring Boot and Angular 2E"))
            .andExpect(jsonPath("$.isbn").value("1234567890"))
            .andExpect(jsonPath("$.authorName").value("Ahmad Gohar"))
            .andExpect(jsonPath("$.price").value(44.99));
    }

    @Test
    void createBook_shouldReturnCreatedBook() throws Exception {
        when(bookService.create(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
            .content(validBookJson()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Spring Boot and Angular 2E"))
            .andExpect(jsonPath("$.isbn").value("1234567890"))
            .andExpect(jsonPath("$.authorName").value("Ahmad Gohar"))
            .andExpect(jsonPath("$.price").value(44.99));
    }

    @Test
    void replaceBook_shouldReturnUpdatedBook() throws Exception {
        when(bookService.replace(anyLong(), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
            .content(validBookJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Spring Boot and Angular 2E"))
            .andExpect(jsonPath("$.isbn").value("1234567890"))
            .andExpect(jsonPath("$.authorName").value("Ahmad Gohar"))
            .andExpect(jsonPath("$.price").value(44.99));
    }

    @Test
    void updateBook_shouldReturnPatchedBook() throws Exception {
        when(bookService.patch(anyLong(), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(patch("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
            .content(validBookJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Spring Boot and Angular 2E"))
            .andExpect(jsonPath("$.isbn").value("1234567890"))
            .andExpect(jsonPath("$.authorName").value("Ahmad Gohar"))
            .andExpect(jsonPath("$.price").value(44.99));
    }

    @Test
    void deleteBook_shouldReturnNoContent() throws Exception {
        doNothing().when(bookService).delete(anyLong());

        mockMvc.perform(delete("/api/books/1"))
            .andExpect(status().isNoContent());
    }
    
@Test
void createBook_withInvalidData_shouldReturnBadRequest() throws Exception {
    // Create an invalid book request
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidBookJson()))
        .andDo(print())
        .andExpect(status().isBadRequest());
}

private String validBookJson() {
    return "{\"title\":\"Spring Boot and Angular 2E\",\"isbn\":\"1234567890\",\"authorName\":\"Ahmad Gohar\",\"price\":44.99}";
}

private String invalidBookJson() {
    return "{\"title\":\"\",\"isbn\":\"1234567890\",\"authorName\":\"Ahmad Gohar\",\"price\":44.99}";
}

}
