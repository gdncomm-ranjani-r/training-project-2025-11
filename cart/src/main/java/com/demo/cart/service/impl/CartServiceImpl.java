package com.demo.cart.service.impl;

import com.demo.cart.DTO.*;
import com.demo.cart.Feign.Feign;
import com.demo.cart.entity.Cart;
import com.demo.cart.entity.CartItem;
import com.demo.cart.repository.CartRepository;
import com.demo.cart.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;
    @Autowired
    Feign productClient;

    @Override
    public CartResponseDTO addToCart(Long userId, AddItemRequestDTO request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());


        BookResponseDTO book = productClient.getBook(request.getBookId());

        CartItem item = CartItem.builder()
                .cartItemId(UUID.randomUUID().toString())
                .bookId(request.getBookId())
                .quantity(request.getQuantity())
                .unitPrice(book.getPrice())
                .totalPrice(book.getPrice() * request.getQuantity())
                .build();

        cart.getItems().add(item);
        Cart saved = cartRepository.save(cart);
        return convertToDTO(saved);
    }

    @Override
    public CartResponseDTO getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart is empty"));

        return convertToDTO(cart);
    }

    @Override
    public CartResponseDTO updateCart(Long userId, UpdateItemRequestDTO request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCartItemId().equals(request.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        BookResponseDTO book = productClient.getBook(item.getBookId());

        item.setQuantity(request.getQuantity());
        item.setTotalPrice(book.getPrice() * request.getQuantity());

        return convertToDTO(cartRepository.save(cart));
    }

    @Override
    public void removeItem(Long userId, String cartItemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getCartItemId().equals(cartItemId));

        cartRepository.save(cart);
    }


    private CartResponseDTO convertToDTO(Cart cart) {

        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());
        response.setUserId(cart.getUserId());

        List<ItemResponseDTO> itemResponses = cart.getItems().stream()
                .map(item -> {
                    BookResponseDTO book = productClient.getBook(item.getBookId());
                    ItemResponseDTO dto = new ItemResponseDTO();
                    BeanUtils.copyProperties(item, dto);
                    dto.setBookDetails(book);
                    return dto;
                })
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        return response;
    }
}
