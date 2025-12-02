package com.demo.product.controller;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookResponseDTO;
import com.demo.product.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")

public class ProductController {

    @Autowired
    BookService bookService;


    @GetMapping("/searchWithFilter")
    public ResponseEntity<Page<BookResponseDTO>> listBooks(
            BookListFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookResponseDTO> response = bookService.getBooks(filter, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookResponseDTO> response = bookService.search(keyword, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable String bookId) {

        BookResponseDTO response = bookService.getBook(bookId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}