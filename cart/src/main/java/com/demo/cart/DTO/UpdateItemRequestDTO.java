package com.demo.cart.DTO;

import lombok.Data;

@Data
public class UpdateItemRequestDTO {
    private String cartItemId;
    private Integer quantity;
}
