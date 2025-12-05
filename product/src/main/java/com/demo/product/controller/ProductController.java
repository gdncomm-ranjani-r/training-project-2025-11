package com.demo.product.controller;

import com.demo.product.DTO.GdnBaseResponse;
import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookRequestDTO;
import com.demo.product.DTO.BookResponseDTO;
import com.demo.product.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/books")
public class ProductController {

    @Autowired
    private BookService bookService;


    @GetMapping("/searchWithFilter")
    public ResponseEntity<GdnBaseResponse<Page<BookResponseDTO>>> listBooks(
            BookListFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received book search request with filter - page: {}, size: {}, filter: {}", 
                page, size, filter);

        if (page < 0) {
            log.warn("Invalid page number: {}", page);
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size: {}", size);
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        try {
            Page<BookResponseDTO> books = bookService.getBooks(filter, page, size);
            log.info("Book search completed - found {} books, total pages: {}", 
                    books.getTotalElements(), books.getTotalPages());
            GdnBaseResponse<Page<BookResponseDTO>> response = GdnBaseResponse.success(books, "Books retrieved successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error during book search with filter", e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<GdnBaseResponse<Page<BookResponseDTO>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received book search request - keyword: {}, page: {}, size: {}", 
                keyword, page, size);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Book search failed: Keyword is missing");
            throw new IllegalArgumentException("Keyword parameter is required for search");
        }
        if (page < 0) {
            log.warn("Invalid page number: {}", page);
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size: {}", size);
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        try {
            Page<BookResponseDTO> books = bookService.search(keyword, page, size);
            log.info("Book search completed for keyword '{}' - found {} books, total pages: {}", 
                    keyword, books.getTotalElements(), books.getTotalPages());
            GdnBaseResponse<Page<BookResponseDTO>> response = GdnBaseResponse.success(books, "Books retrieved successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error during book search for keyword: {}", keyword, e);
            throw e;
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<GdnBaseResponse<BookResponseDTO>> getBook(@PathVariable String bookId) {

        log.info("Received request to get book details for bookId: {}", bookId);

        if (bookId == null || bookId.trim().isEmpty()) {
            log.warn("Get book request failed: Book ID is missing");
            throw new IllegalArgumentException("Book ID is required");
        }

        try {
            BookResponseDTO book = bookService.getBook(bookId);
            log.info("Book details retrieved successfully for bookId: {}, title: {}", 
                    bookId, book.getTitle());
            GdnBaseResponse<BookResponseDTO> response = GdnBaseResponse.success(book, "Book retrieved successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving book details for bookId: {}", bookId, e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<GdnBaseResponse<BookResponseDTO>> createBook(@RequestBody BookRequestDTO request) {
        log.info("Received request to create book - title: {}", request != null ? request.getTitle() : "null");

        if (request == null) {
            log.warn("Create book request failed: Request body is missing");
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("Create book request failed: Title is missing");
            throw new IllegalArgumentException("Title is required");
        }

        try {
            BookResponseDTO book = bookService.createBook(request);
            log.info("Book created successfully with bookId: {}, title: {}", 
                    book.getId(), book.getTitle());
            GdnBaseResponse<BookResponseDTO> response = GdnBaseResponse.success(book, "Book created successfully", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating book", e);
            throw e;
        }
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<GdnBaseResponse<BookResponseDTO>> updateBook(
            @PathVariable String bookId,
            @RequestBody BookRequestDTO request) {
        log.info("Received request to update book - bookId: {}", bookId);

        if (bookId == null || bookId.trim().isEmpty()) {
            log.warn("Update book request failed: Book ID is missing");
            throw new IllegalArgumentException("Book ID is required");
        }
        if (request == null) {
            log.warn("Update book request failed: Request body is missing");
            throw new IllegalArgumentException("Request body is required");
        }

        try {
            BookResponseDTO book = bookService.updateBook(bookId, request);
            log.info("Book updated successfully for bookId: {}, title: {}", 
                    bookId, book.getTitle());
            GdnBaseResponse<BookResponseDTO> response = GdnBaseResponse.success(book, "Book updated successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating book for bookId: {}", bookId, e);
            throw e;
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<GdnBaseResponse<String>> deleteBook(@PathVariable String bookId) {
        log.info("Received request to delete book - bookId: {}", bookId);

        if (bookId == null || bookId.trim().isEmpty()) {
            log.warn("Delete book request failed: Book ID is missing");
            throw new IllegalArgumentException("Book ID is required");
        }

        try {
            bookService.deleteBook(bookId);
            log.info("Book deleted successfully for bookId: {}", bookId);
            GdnBaseResponse<String> response = GdnBaseResponse.success("Book deleted successfully", "Book deleted successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting book for bookId: {}", bookId, e);
            throw e;
        }
    }
}
