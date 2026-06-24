package com.learningfever.library.service;

import com.learningfever.library.config.CacheConfig;
import com.learningfever.library.entity.Book;
import com.learningfever.library.exception.DomainException;
import com.learningfever.library.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// @Cacheable caches the method return value (a detached JPA entity or a DTO) keyed by argument.
// Hibernate's second-level cache (L2C) caches entity state inside the persistence context and is
// shared across sessions. We use Spring's @Cacheable here (not Hibernate L2C) because Book is a
// frequently-read aggregate whose cached form is a plain Java object consumed by the API layer.
// Caching a JPA-managed (attached) entity in Spring's cache is a footgun: the cache outlives the
// EntityManager, so the cached entity has no proxy, lazy collections will throw
// LazyInitializationException on access, and concurrent writes to the underlying row will not
// invalidate the Spring cache entry — you must evict manually. We return plain Book entities
// only within a transaction; callers that need a cached view should use a DTO-based service method.
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Cacheable(value = CacheConfig.BOOKS_CACHE, key = "#bookId")
    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new DomainException("BOOK_NOT_FOUND", "Book not found: " + bookId));
    }

    @CacheEvict(value = CacheConfig.BOOKS_CACHE, key = "#bookId")
    public void evictBook(Long bookId) {
        // Called explicitly after borrow or return so the cached availability count is refreshed.
    }
}