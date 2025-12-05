package com.demo.cart.controller;

import com.demo.cart.DTO.AddItemRequestDTO;
import com.demo.cart.DTO.AddToCartResponseDTO;
import com.demo.cart.DTO.GdnBaseResponse;
import com.demo.cart.DTO.CartResponseDTO;
import com.demo.cart.DTO.UpdateItemRequestDTO;
import com.demo.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<GdnBaseResponse<AddToCartResponseDTO>> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId, 
            @RequestBody(required = false) AddItemRequestDTO request) {
        
        log.info("Received add to cart request - userId: {}, bookId: {}, quantity: {}", 
                userId, request != null ? request.getBookId() : "null", 
                request != null ? request.getQuantity() : "null");
        
        if (userId == null) {
            log.warn("Add to cart failed: X-User-Id header is missing");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        if (request == null) {
            log.warn("Add to cart failed: Request body is missing for userId: {}", userId);
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
            log.warn("Add to cart failed: Book ID is missing for userId: {}", userId);
            throw new IllegalArgumentException("Book ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Add to cart failed: Invalid quantity {} for userId: {}, bookId: {}", 
                    request.getQuantity(), userId, request.getBookId());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        try {
            cartService.addToCart(userId, request);
            log.info("Item added to cart successfully - userId: {}, bookId: {}, quantity: {}", 
                    userId, request.getBookId(), request.getQuantity());
            
            AddToCartResponseDTO addToCartResponse = new AddToCartResponseDTO(
                    request.getBookId(),
                    request.getQuantity()
            );
            
            GdnBaseResponse<AddToCartResponseDTO> response = GdnBaseResponse.success(addToCartResponse, "Item added to cart successfully", HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error adding item to cart - userId: {}, bookId: {}", 
                    userId, request.getBookId(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<GdnBaseResponse<CartResponseDTO>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("Received get cart request for userId: {}", userId);
        
        if (userId == null) {
            log.warn("Get cart failed: X-User-Id header is missing");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        
        try {
            CartResponseDTO cart = cartService.getCart(userId);
            log.info("Cart retrieved successfully for userId: {}, items count: {}", 
                    userId, cart.getItems() != null ? cart.getItems().size() : 0);
            GdnBaseResponse<CartResponseDTO> response = GdnBaseResponse.success(cart, "Cart retrieved successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving cart for userId: {}", userId, e);
            throw e;
        }
    }

    @PutMapping("/update")
    public ResponseEntity<GdnBaseResponse<CartResponseDTO>> updateCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId, 
            @RequestBody(required = false) UpdateItemRequestDTO request) {
        
        log.info("Received update cart request - userId: {}, cartItemId: {}, quantity: {}", 
                userId, request != null ? request.getCartItemId() : "null", 
                request != null ? request.getQuantity() : "null");
        
        if (userId == null) {
            log.warn("Update cart failed: X-User-Id header is missing");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        if (request == null) {
            log.warn("Update cart failed: Request body is missing for userId: {}", userId);
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getCartItemId() == null || request.getCartItemId().trim().isEmpty()) {
            log.warn("Update cart failed: Cart Item ID is missing for userId: {}", userId);
            throw new IllegalArgumentException("Cart Item ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Update cart failed: Invalid quantity {} for userId: {}, cartItemId: {}", 
                    request.getQuantity(), userId, request.getCartItemId());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        try {
            CartResponseDTO cart = cartService.updateCart(userId, request);
            log.info("Cart updated successfully - userId: {}, cartItemId: {}, new quantity: {}", 
                    userId, request.getCartItemId(), request.getQuantity());
            GdnBaseResponse<CartResponseDTO> response = GdnBaseResponse.success(cart, "Cart updated successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating cart - userId: {}, cartItemId: {}", 
                    userId, request.getCartItemId(), e);
            throw e;
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<GdnBaseResponse<String>> removeItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId, 
            @PathVariable String cartItemId) {
        
        log.info("Received remove item request - userId: {}, cartItemId: {}", userId, cartItemId);
        
        if (userId == null) {
            log.warn("Remove item failed: X-User-Id header is missing");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        if (cartItemId == null || cartItemId.trim().isEmpty()) {
            log.warn("Remove item failed: Cart Item ID is missing for userId: {}", userId);
            throw new IllegalArgumentException("Cart Item ID is required");
        }
        
        try {
            cartService.removeItem(userId, cartItemId);
            log.info("Item removed successfully from cart - userId: {}, cartItemId: {}", 
                    userId, cartItemId);
            GdnBaseResponse<String> response = GdnBaseResponse.success("Item removed successfully", "Item removed successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error removing item from cart - userId: {}, cartItemId: {}", 
                    userId, cartItemId, e);
            throw e;
        }
    }
}


