package com.demo.product.repository;

import com.demo.product.entity.Books;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookServiceRepository extends MongoRepository<Books,String> {
    List<Books> findByTitleContainingIgnoreCase(String title);
    List<Books> findByAuthorContainingIgnoreCase(String author);
    List<Books> findByPublisherContainingIgnoreCase(String publisher);
}
