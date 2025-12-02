package com.demo.cart.DTO;

import lombok.Data;

@Data
public class BookResponseDTO {
    private String id;
    private String title;
    private String author;
    private Double price;
    private Double rating;
}
