package com.learningfever.library.service;

import com.learningfever.library.dto.*;
import com.learningfever.library.entity.*;
import com.learningfever.library.repository.BookRepository;
import com.learningfever.library.search.BookSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchService {

    private final BookRepository bookRepository;

    public SearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Two-query pagination: fetch matching IDs first (with pagination), then hydrate entities
    // to avoid Hibernate's in-memory pagination warning when fetch joins are combined with LIMIT.
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        Specification<Book> spec = BookSpecifications.from(criteria);

        Page<Long> idPage = bookRepository.findAll(spec, pageable).map(Book::getId);
        if (idPage.isEmpty()) return Page.empty(pageable);

        List<Book> books = bookRepository.findAllByIdsWithAuthors(idPage.getContent());

        List<BookSummaryDto> summaries = books.stream()
            .map(b -> new BookSummaryDto(
                b.getId(),
                b.getTitle(),
                b.getPrimaryAuthor().map(Author::getFullName).orElse("Unknown"),
                b.getAvailableCopies()
            ))
            .toList();

        return new PageImpl<>(summaries, pageable, idPage.getTotalElements());
    }
}