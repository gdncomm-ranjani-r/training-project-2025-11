package com.demo.product.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class BookListFilterDTO implements Serializable  {
    private static final long serialVersionUID = 1L;
    private String title;
    private String author;
    private String publisher;
    private String format;

    private Double minPrice;
    private Double maxPrice;

    private Double minRating;
    private Double maxRating;
}
