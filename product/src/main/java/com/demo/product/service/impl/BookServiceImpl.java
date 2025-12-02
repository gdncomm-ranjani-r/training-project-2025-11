package com.demo.product.service.impl;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookResponseDTO;
import com.demo.product.entity.Books;
import com.demo.product.repository.BookSearchRepository;
import com.demo.product.repository.BookServiceRepository;
import com.demo.product.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    BookServiceRepository bookRepository;

    @Autowired
    BookSearchRepository searchRepository;

    @Override
    public Page<BookResponseDTO> getBooks(BookListFilterDTO filter, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Books> books = searchRepository.filterBooks(filter, pageable);

        return books.map(this::convertToDto);
    }

    @Override
    public Page<BookResponseDTO> search(String keyword, int page, int size) {

        List<Books> merged = new ArrayList<>();

        merged.addAll(bookRepository.findByTitleContainingIgnoreCase(keyword));
        merged.addAll(bookRepository.findByAuthorContainingIgnoreCase(keyword));
        merged.addAll(bookRepository.findByPublisherContainingIgnoreCase(keyword));

        List<Books> distinct = merged.stream()
                .distinct()
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);

        int start = Math.min((int) pageable.getOffset(), distinct.size());
        int end = Math.min(start + pageable.getPageSize(), distinct.size());

        Page<Books> pageObj = new PageImpl<>(distinct.subList(start, end), pageable, distinct.size());

        return pageObj.map(this::convertToDto);
    }

    @Override
    public BookResponseDTO getBook(String bookId) {
        Books book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book Not Found"));
        return convertToDto(book);
    }


    private BookResponseDTO convertToDto(Books book) {
        BookResponseDTO dto = new BookResponseDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }
}

