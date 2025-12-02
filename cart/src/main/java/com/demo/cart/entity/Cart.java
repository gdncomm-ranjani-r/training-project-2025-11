package com.demo.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.util.List;


@Data
@Document(collection = "cart")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {


    @Id
    private String id;

    private Long userId;
    private List<CartItem> items;
}
