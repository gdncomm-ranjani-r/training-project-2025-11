package com.demo.cart.service;

import com.demo.cart.DTO.AddItemRequestDTO;
import com.demo.cart.DTO.BookResponseDTO;
import com.demo.cart.DTO.CartResponseDTO;
import com.demo.cart.DTO.GdnBaseResponse;
import com.demo.cart.DTO.UpdateItemRequestDTO;
import com.demo.cart.Feign.Feign;
import com.demo.cart.entity.Cart;
import com.demo.cart.entity.CartItem;
import com.demo.cart.exception.ResourceNotFoundException;
import com.demo.cart.repository.CartRepository;
import com.demo.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private Feign productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Long userId;
    private AddItemRequestDTO addItemRequest;
    private BookResponseDTO bookResponse;
    private GdnBaseResponse<BookResponseDTO> bookGdnResponse;
    private Cart existingCart;
    private CartItem existingCartItem;

    @BeforeEach
    void setUp() {
        userId = 1L;

        addItemRequest = new AddItemRequestDTO();
        addItemRequest.setBookId("BOOK-123456");
        addItemRequest.setQuantity(2);

        bookResponse = new BookResponseDTO();
        bookResponse.setId("BOOK-123456");
        bookResponse.setTitle("Test Book");
        bookResponse.setPrice(99.99);

        bookGdnResponse = new GdnBaseResponse<>();
        bookGdnResponse.setSuccess(true);
        bookGdnResponse.setData(bookResponse);

        existingCartItem = CartItem.builder()
                .cartItemId("cart-item-1")
                .bookId("BOOK-123456")
                .quantity(3)
                .unitPrice(99.99)
                .totalPrice(299.97)
                .build();

        existingCart = Cart.builder()
                .id("cart-1")
                .userId(userId)
                .items(new ArrayList<>(Arrays.asList(existingCartItem)))
                .build();
    }

    @Test
    void testAddToCart_NewCart_NewItem() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productClient.getBook("BOOK-123456")).thenReturn(bookGdnResponse);
        when(cartRepository.save(any(Cart.class))).thenAnswer(test -> {
            Cart cart = test.getArgument(0);
            cart.setId("cart-1");
            return cart;
        });

        cartService.addToCart(userId, addItemRequest);

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productClient, times(1)).getBook("BOOK-123456");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddToCart_ExistingCart_NewItem() {
        Cart cartWithOtherItems = Cart.builder()
                .id("cart-1")
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartWithOtherItems));
        when(productClient.getBook("BOOK-123456")).thenReturn(bookGdnResponse);
        when(cartRepository.save(any(Cart.class))).thenReturn(cartWithOtherItems);

        cartService.addToCart(userId, addItemRequest);

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productClient, times(1)).getBook("BOOK-123456");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddToCart_ExistingCart_ExistingItem() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(productClient.getBook("BOOK-123456")).thenReturn(bookGdnResponse);
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        cartService.addToCart(userId, addItemRequest);

        assertEquals(5, existingCartItem.getQuantity());
        assertEquals(99.99, existingCartItem.getUnitPrice());
        assertEquals(499.95, existingCartItem.getTotalPrice());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productClient, times(1)).getBook("BOOK-123456");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testGetCart_NoCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNull(result.getCartId());
        assertTrue(result.getItems().isEmpty());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productClient, never()).getBook(anyString());
    }

}

