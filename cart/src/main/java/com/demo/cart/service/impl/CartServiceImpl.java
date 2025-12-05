package com.demo.cart.service.impl;

import com.demo.cart.DTO.*;
import com.demo.cart.Feign.Feign;
import com.demo.cart.entity.Cart;
import com.demo.cart.entity.CartItem;
import com.demo.cart.exception.ResourceNotFoundException;
import com.demo.cart.repository.CartRepository;
import com.demo.cart.service.CartService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;
    @Autowired
    Feign productClient;

    @Override
    public void addToCart(Long userId, AddItemRequestDTO request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getBookId().equals(request.getBookId()))
                .findFirst()
                .orElse(null);

        GdnBaseResponse<BookResponseDTO> bookResponse = productClient.getBook(request.getBookId());
        if (bookResponse == null || !bookResponse.isSuccess() || bookResponse.getData() == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + request.getBookId());
        }
        BookResponseDTO book = bookResponse.getData();
        Double currentPrice = book.getPrice();

        if (existingItem != null) {
            log.info("Book {} already exists in cart for userId: {}, updating quantity from {} to {}",
                    request.getBookId(), userId, existingItem.getQuantity(), 
                    existingItem.getQuantity() + request.getQuantity());
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            existingItem.setQuantity(newQuantity);
            
            existingItem.setUnitPrice(currentPrice);
            existingItem.setTotalPrice(currentPrice * newQuantity);
        } else {
            log.info("Adding new book {} to cart for userId: {} with quantity: {}",
                    request.getBookId(), userId, request.getQuantity());
            
            CartItem newItem = CartItem.builder()
                    .cartItemId(UUID.randomUUID().toString())
                    .bookId(request.getBookId())
                    .quantity(request.getQuantity())
                    .unitPrice(currentPrice)
                    .totalPrice(currentPrice * request.getQuantity())
                    .build();

            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Override
    public CartResponseDTO getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            CartResponseDTO empty = new CartResponseDTO();
            empty.setUserId(userId);
            empty.setItems(new ArrayList<>());
            empty.setCartId(null);
            return empty;
        }
        
        boolean cartUpdated = cleanupInvalidCartItems(cart);
        boolean pricesUpdated = refreshCartItemPrices(cart);
        
        if (cartUpdated || pricesUpdated) {
            cart = cartRepository.save(cart);
            if (pricesUpdated) {
                log.info("Refreshed prices for cart items for userId: {}", userId);
            }
            if (cartUpdated) {
                log.info("Cleaned up invalid cart items for userId: {}", userId);
            }
        }
        return convertToDTO(cart);
    }

    @Override
    public CartResponseDTO updateCart(Long userId, UpdateItemRequestDTO request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCartItemId().equals(request.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + request.getCartItemId()));
        try {
            GdnBaseResponse<BookResponseDTO> bookResponse = productClient.getBook(item.getBookId());
            if (bookResponse == null || !bookResponse.isSuccess() || bookResponse.getData() == null) {
                throw new ResourceNotFoundException("Book not found with ID: " + item.getBookId());
            }
            BookResponseDTO book = bookResponse.getData();
            item.setUnitPrice(book.getPrice());
            item.setQuantity(request.getQuantity());
            item.setTotalPrice(book.getPrice() * request.getQuantity());
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("Book {} no longer exists, removing cart item {} for userId: {}",
                        item.getBookId(), request.getCartItemId(), userId);
                cart.getItems().removeIf(i -> i.getCartItemId().equals(request.getCartItemId()));
                cartRepository.save(cart);
                throw new ResourceNotFoundException("Book no longer exists. Cart item has been removed.");
            }
            throw e;
        }
        return convertToDTO(cartRepository.save(cart));
    }

    @Override
    public void removeItem(Long userId, String cartItemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        cart.getItems().removeIf(item -> item.getCartItemId().equals(cartItemId));

        cartRepository.save(cart);
    }

    private boolean refreshCartItemPrices(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return false;
        }

        boolean pricesUpdated = false;
        
        for (CartItem item : cart.getItems()) {
            try {
                GdnBaseResponse<BookResponseDTO> bookResponse = productClient.getBook(item.getBookId());
                if (bookResponse == null || !bookResponse.isSuccess() || bookResponse.getData() == null) {
                    log.warn("Book {} not found while refreshing prices, will be removed by cleanup",
                            item.getBookId());
                    continue;
                }
                BookResponseDTO book = bookResponse.getData();
                Double currentPrice = book.getPrice();
                if (!currentPrice.equals(item.getUnitPrice())) {
                    log.info("Price updated for book {}: old price={}, new price={}, userId={}", 
                            item.getBookId(), item.getUnitPrice(), currentPrice, cart.getUserId());
                    item.setUnitPrice(currentPrice);
                    item.setTotalPrice(currentPrice * item.getQuantity());
                    pricesUpdated = true;
                }
            } catch (FeignException e) {
                if (e.status() == 404) {
                    log.warn("Book {} not found while refreshing prices, will be removed by cleanup",
                            item.getBookId());
                } else {
                    log.error("Error refreshing price for book {}: {}", item.getBookId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("Unexpected error refreshing price for book {}: {}", item.getBookId(), e.getMessage());
            }
        }
        
        return pricesUpdated;
    }

    private boolean cleanupInvalidCartItems(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return false;
        }

        List<CartItem> itemsToRemove = new ArrayList<>();
        
        for (CartItem item : cart.getItems()) {
            try {
                GdnBaseResponse<BookResponseDTO> bookResponse = productClient.getBook(item.getBookId());
                if (bookResponse == null || !bookResponse.isSuccess() || bookResponse.getData() == null) {
                    log.warn("Book {} no longer exists, removing from cart for userId: {}",
                            item.getBookId(), cart.getUserId());
                    itemsToRemove.add(item);
                    continue;
                }
            } catch (FeignException e) {
                if (e.status() == 404) {
                    log.warn("Book {} no longer exists, removing from cart for userId: {}",
                            item.getBookId(), cart.getUserId());
                    itemsToRemove.add(item);
                } else {
                    log.error("Error checking book {} for cart item: {}", item.getBookId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("Unexpected error checking book {} for cart item: {}", item.getBookId(), e.getMessage());
            }
        }

        if (!itemsToRemove.isEmpty()) {
            cart.getItems().removeAll(itemsToRemove);
            return true;
        }
        
        return false;
    }

    private CartResponseDTO convertToDTO(Cart cart) {

        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());
        response.setUserId(cart.getUserId());

        List<ItemResponseDTO> itemResponses = cart.getItems().stream()
                .map(item -> {
                    try {
                        GdnBaseResponse<BookResponseDTO> bookResponse = productClient.getBook(item.getBookId());
                        if (bookResponse == null || !bookResponse.isSuccess() || bookResponse.getData() == null) {
                            log.warn("Book {} not found while converting cart to DTO for userId: {}", 
                                    item.getBookId(), cart.getUserId());
                            return null; // Will be filtered out
                        }
                        BookResponseDTO book = bookResponse.getData();
                        ItemResponseDTO dto = new ItemResponseDTO();
                        BeanUtils.copyProperties(item, dto);
                        dto.setBookDetails(book);
                        return dto;
                    } catch (FeignException e) {
                        if (e.status() == 404) {
                            // Book doesn't exist - this shouldn't happen if cleanupInvalidCartItems was called
                            log.warn("Book {} not found while converting cart to DTO for userId: {}", 
                                    item.getBookId(), cart.getUserId());
                            return null; // Will be filtered out
                        }
                        throw e;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        return response;
    }
}
