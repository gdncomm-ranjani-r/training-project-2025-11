package com.demo.product.DTO;

import lombok.Data;

@Data
public class BookListFilterDTO {

    private String title;
    private String author;
    private String publisher;
    private String format;

    private Double minPrice;
    private Double maxPrice;

    private Double minRating;
    private Double maxRating;
}
