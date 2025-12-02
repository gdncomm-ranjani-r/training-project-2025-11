package com.demo.cart.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CartResponseDTO {
    private String cartId;
    private Long userId;
    private List<ItemResponseDTO> items;
}
