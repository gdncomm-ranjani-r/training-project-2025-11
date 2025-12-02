package com.demo.product.service;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookResponseDTO;
import org.springframework.data.domain.Page;

public interface BookService {
    Page<BookResponseDTO> getBooks(BookListFilterDTO filter, int page, int size);
    Page<BookResponseDTO> search(String keyword, int page, int size);
    BookResponseDTO getBook(String bookId);
}
