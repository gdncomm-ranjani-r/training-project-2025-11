package com.demo.product.DTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BookResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;

    private String title;
    private String subtitle;

    private String author;
    private List<String> coAuthors;

    private String description;
    private String publisher;
    private Date publishedDate;

    private String edition;
    private Integer pageCount;
    private String format;

    private Double price;
    private Double mrpPrice;

    private Integer stockAvailable;
    private Double rating;
}
