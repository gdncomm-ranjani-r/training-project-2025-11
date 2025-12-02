package com.demo.product.repository;

import com.demo.product.DTO.BookListFilterDTO;
import com.demo.product.entity.Books;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BookSearchRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public Page<Books> filterBooks(BookListFilterDTO filter, Pageable pageable) {

        List<Criteria> conditions = new ArrayList<>();

        if (filter.getTitle() != null)
            conditions.add(Criteria.where("title").regex(filter.getTitle(), "i"));

        if (filter.getAuthor() != null)
            conditions.add(Criteria.where("author").regex(filter.getAuthor(), "i"));

        if (filter.getPublisher() != null)
            conditions.add(Criteria.where("publisher").regex(filter.getPublisher(), "i"));

        if (filter.getFormat() != null)
            conditions.add(Criteria.where("format").regex(filter.getFormat(), "i"));

        if (filter.getMinPrice() != null)
            conditions.add(Criteria.where("price").gte(filter.getMinPrice()));

        if (filter.getMaxPrice() != null)
            conditions.add(Criteria.where("price").lte(filter.getMaxPrice()));

        if (filter.getMinRating() != null)
            conditions.add(Criteria.where("rating").gte(filter.getMinRating()));

        if (filter.getMaxRating() != null)
            conditions.add(Criteria.where("rating").lte(filter.getMaxRating()));

        Query q = new Query();

        if (!conditions.isEmpty())
            q.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));

        q.with(pageable);

        List<Books> result = mongoTemplate.find(q, Books.class);
        long total = mongoTemplate.count(Query.of(q).limit(-1).skip(-1), Books.class);

        return new PageImpl<>(result, pageable, total);
    }
}
