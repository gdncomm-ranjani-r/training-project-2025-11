package com.demo.product.service;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.DTO.BookRequestDTO;
import com.demo.product.DTO.BookResponseDTO;
import com.demo.product.entity.Books;
import com.demo.product.exception.ResourceNotFoundException;
import com.demo.product.repository.BookSearchRepository;
import com.demo.product.repository.BookServiceRepository;
import com.demo.product.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookServiceRepository bookRepository;

    @Mock
    private BookSearchRepository searchRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookRequestDTO bookRequest;
    private Books savedBook;
    private BookResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        bookRequest = new BookRequestDTO();
        bookRequest.setTitle("Test Book");
        bookRequest.setAuthor("Test Author");
        bookRequest.setPublisher("Test Publisher");
        bookRequest.setPrice(99.99);
        bookRequest.setDescription("Test Description");

        savedBook = Books.builder()
                .id("BOOK-123456")
                .title("Test Book")
                .author("Test Author")
                .publisher("Test Publisher")
                .price(99.99)
                .description("Test Description")
                .build();

        expectedResponse = new BookResponseDTO();
        expectedResponse.setId("BOOK-123456");
        expectedResponse.setTitle("Test Book");
        expectedResponse.setAuthor("Test Author");
        expectedResponse.setPublisher("Test Publisher");
        expectedResponse.setPrice(99.99);
        expectedResponse.setDescription("Test Description");
    }

    @Test
    void testGetBook_Success() {
        String bookId = "BOOK-123456";
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(savedBook));

        BookResponseDTO result = bookService.getBook(bookId);

        assertNotNull(result);
        assertEquals("BOOK-123456", result.getId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals(99.99, result.getPrice());

        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void testGetBook_NotFound() {
        String bookId = "BOOK-999999";
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.getBook(bookId);
        });

        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void testSearch_Success() {
        String keyword = "test";
        int page = 0;
        int size = 10;

        List<Books> booksByTitle = Arrays.asList(savedBook);
        List<Books> booksByAuthor = Arrays.asList(savedBook);
        List<Books> booksByPublisher = Arrays.asList(savedBook);

        when(bookRepository.findByTitleContainingIgnoreCase(keyword)).thenReturn(booksByTitle);
        when(bookRepository.findByAuthorContainingIgnoreCase(keyword)).thenReturn(booksByAuthor);
        when(bookRepository.findByPublisherContainingIgnoreCase(keyword)).thenReturn(booksByPublisher);

        Page<BookResponseDTO> result = bookService.search(keyword, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Test Book", result.getContent().get(0).getTitle());

        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(keyword);
        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCase(keyword);
        verify(bookRepository, times(1)).findByPublisherContainingIgnoreCase(keyword);
    }


    @Test
    void testGetBooks_WithFilter() {
        int page = 0;
        int size = 10;
        BookListFilterDTO filter = new BookListFilterDTO();
        filter.setTitle("Test");
        filter.setMinPrice(50.0);
        filter.setMaxPrice(200.0);

        Pageable pageable = PageRequest.of(page, size);
        Page<Books> booksPage = new PageImpl<>(Arrays.asList(savedBook), pageable, 1);

        when(searchRepository.filterBooks(filter, pageable)).thenReturn(booksPage);

        Page<BookResponseDTO> result = bookService.getBooks(filter, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(searchRepository, times(1)).filterBooks(filter, pageable);
    }

    @Test
    void testGetBooks_EmptyResults() {
        int page = 0;
        int size = 10;
        BookListFilterDTO filter = new BookListFilterDTO();

        Pageable pageable = PageRequest.of(page, size);
        Page<Books> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(searchRepository.filterBooks(filter, pageable)).thenReturn(emptyPage);

        Page<BookResponseDTO> result = bookService.getBooks(filter, page, size);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}

