package com.demo.cart.DTO;

import lombok.Data;

@Data
public class AddItemRequestDTO {
    private String bookId;
    private Integer quantity;
}