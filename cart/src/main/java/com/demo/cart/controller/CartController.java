package com.demo.cart.controller;

import com.demo.cart.DTO.AddItemRequestDTO;
import com.demo.cart.DTO.CartResponseDTO;
import com.demo.cart.DTO.UpdateItemRequestDTO;
import com.demo.cart.service.CartService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

        @Autowired
        CartService cartService;

        @PostMapping("/add")
        public ResponseEntity<CartResponseDTO> addToCart(@RequestHeader("X-User-Id") Long userId, @RequestBody AddItemRequestDTO request) {

            CartResponseDTO response = cartService.addToCart(userId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @GetMapping
        public ResponseEntity<CartResponseDTO> getCart(@RequestHeader("X-User-Id") Long userId) {
            CartResponseDTO response = cartService.getCart(userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @PutMapping("/update")
        public ResponseEntity<CartResponseDTO> updateCart(@RequestHeader("X-User-Id") Long userId, @RequestBody UpdateItemRequestDTO request) {
            CartResponseDTO response = cartService.updateCart(userId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @DeleteMapping("/{cartItemId}")
        public ResponseEntity<String> removeItem(@RequestHeader("X-User-Id") Long userId, @PathVariable String cartItemId) {
            cartService.removeItem(userId, cartItemId);
            return new ResponseEntity<>("Item removed successfully", HttpStatus.OK);
        }
    }

