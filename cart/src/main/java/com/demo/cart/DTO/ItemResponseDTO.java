package com.demo.cart.DTO;

import lombok.Data;

@Data
public class ItemResponseDTO {

    private String cartItemId;
    private String bookId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private BookResponseDTO bookDetails;
}
