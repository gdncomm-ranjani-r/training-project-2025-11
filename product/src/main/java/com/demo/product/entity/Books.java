package com.demo.product.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "books")
public class Books{

    @Id
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
