package com.learningfever.library.search;

import com.learningfever.library.entity.*;
import com.learningfever.library.dto.BookSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<Book> titleLike(String pattern) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("title")), "%" + pattern.toLowerCase() + "%");
    }

    public static Specification<Book> authorNameLike(String pattern) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Book, BookAuthor> ba = root.join("bookAuthors", JoinType.INNER);
            Join<BookAuthor, Author> author = ba.join("author", JoinType.INNER);
            String lower = "%" + pattern.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(author.get("firstName")), lower),
                cb.like(cb.lower(author.get("lastName")), lower),
                cb.like(cb.lower(cb.concat(cb.concat(author.get("firstName"), " "), author.get("lastName"))), lower)
            );
        };
    }

    public static Specification<Book> inCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Book> priceCeiling(Money ceiling) {
        return (root, query, cb) -> {
            Path<BigDecimal> amount = root.get("price").get("amount");
            Path<String> currency = root.get("price").get("currency");
            return cb.and(
                cb.lessThanOrEqualTo(amount, ceiling.getAmount()),
                cb.equal(currency, ceiling.getCurrency())
            );
        };
    }

    public static Specification<Book> availableOnly() {
        return (root, query, cb) -> cb.greaterThan(root.get("availableCopies"), 0);
    }

    public static Specification<Book> publishedAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("publicationDate"), date);
    }

    public static Specification<Book> publishedBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("publicationDate"), date);
    }

    public static Specification<Book> from(BookSearchCriteria criteria) {
        Specification<Book> spec = Specification.where(null);
        if (criteria.titleLike() != null && !criteria.titleLike().isBlank())
            spec = spec.and(titleLike(criteria.titleLike()));
        if (criteria.authorNameLike() != null && !criteria.authorNameLike().isBlank())
            spec = spec.and(authorNameLike(criteria.authorNameLike()));
        if (criteria.categoryId() != null)
            spec = spec.and(inCategory(criteria.categoryId()));
        if (criteria.priceCeiling() != null)
            spec = spec.and(priceCeiling(criteria.priceCeiling()));
        if (Boolean.TRUE.equals(criteria.availableOnly()))
            spec = spec.and(availableOnly());
        if (criteria.publishedAfter() != null)
            spec = spec.and(publishedAfter(criteria.publishedAfter()));
        if (criteria.publishedBefore() != null)
            spec = spec.and(publishedBefore(criteria.publishedBefore()));
        return spec;
    }
}