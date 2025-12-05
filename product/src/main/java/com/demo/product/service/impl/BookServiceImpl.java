package com.demo.product.service.impl;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookRequestDTO;
import com.demo.product.DTO.BookResponseDTO;
import com.demo.product.entity.Books;
import com.demo.product.exception.ResourceNotFoundException;
import com.demo.product.repository.BookSearchRepository;
import com.demo.product.repository.BookServiceRepository;
import com.demo.product.service.BookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Random;
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
    @Cacheable(value = "books", key = "'search:' + #keyword + ':page:' + #page + ':size:' + #size")
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
    @Cacheable(value = "books", key = "#bookId")
    public BookResponseDTO getBook(String bookId) {
        Books book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        return convertToDto(book);
    }

    @Override
    @CachePut(value = "books", key = "#result.id")
    public BookResponseDTO createBook(BookRequestDTO request) {
        validateBookRequest(request);
        
        Books book = convertToEntity(request);
        
        String bookId = generateBookId();
        while (bookRepository.findById(bookId).isPresent()) {
            bookId = generateBookId();
        }
        
        book.setId(bookId);
        Books saved = bookRepository.save(book);
        return convertToDto(saved);
    }
    
    private String generateBookId() {
        int randomNumber = new Random().nextInt(900000) + 100000;
        return "BOOK-" + randomNumber;
    }

    @Override
    @CachePut(value = "books", key = "#bookId")
    public BookResponseDTO updateBook(String bookId, BookRequestDTO request) {
        Books existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        validateBookRequest(request);
        updateEntityFromRequest(existingBook, request);
        Books updated = bookRepository.save(existingBook);
        return convertToDto(updated);
    }

    @Override
    @CacheEvict(value = "books", key = "#bookId")
    public void deleteBook(String bookId) {
        Books book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        bookRepository.delete(book);
    }

    private BookResponseDTO convertToDto(Books book) {
        BookResponseDTO dto = new BookResponseDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }

    private Books convertToEntity(BookRequestDTO dto) {
        Books book = new Books();
        BeanUtils.copyProperties(dto, book);
        return book;
    }

    private void validateBookRequest(BookRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required and cannot be empty");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required and cannot be empty");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("Price is required and must be non-negative");
        }
        if (request.getPublisher() == null || request.getPublisher().trim().isEmpty()) {
            throw new IllegalArgumentException("Publisher is required and cannot be empty");
        }
    }

    private void updateEntityFromRequest(Books book, BookRequestDTO request) {

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setPublisher(request.getPublisher());
        //Optional Parameters
        if (request.getSubtitle() != null) book.setSubtitle(request.getSubtitle());
        if (request.getCoAuthors() != null) book.setCoAuthors(request.getCoAuthors());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getPublishedDate() != null) book.setPublishedDate(request.getPublishedDate());
        if (request.getEdition() != null) book.setEdition(request.getEdition());
        if (request.getPageCount() != null) book.setPageCount(request.getPageCount());
        if (request.getFormat() != null) book.setFormat(request.getFormat());
        if (request.getMrpPrice() != null) book.setMrpPrice(request.getMrpPrice());
        if (request.getStockAvailable() != null) book.setStockAvailable(request.getStockAvailable());
        if (request.getRating() != null) book.setRating(request.getRating());
    }
}

