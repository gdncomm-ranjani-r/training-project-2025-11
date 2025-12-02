package com.demo.cart.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItem {

    private String cartItemId;
    private String bookId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}