package com.demo.cart.Feign;

import com.demo.cart.DTO.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product", url = "http://localhost:8015")
public interface Feign {

    @GetMapping("/books/{bookId}")
    BookResponseDTO getBook(@PathVariable("bookId") String bookId);
}
