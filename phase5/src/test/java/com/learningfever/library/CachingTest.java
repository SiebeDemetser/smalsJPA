package com.learningfever.library;

import com.learningfever.library.entity.*;
import com.learningfever.library.repository.*;
import com.learningfever.library.service.BookService;
import com.learningfever.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CachingTest {

    @Autowired
    BookService bookService;

    @Autowired
    LoanService loanService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PublisherRepository publisherRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    private Long bookId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("books").clear();
        transactionTemplate.execute(status -> {
            Category cat = categoryRepository.save(new Category("Test Cat", "desc"));
            Publisher pub = publisherRepository.save(new Publisher("Test Pub", "Addr", "p@test.com"));
            Book book = bookRepository.save(new Book(
                "978-CACHE-001", "Cacheable Book", 2,
                Money.of("15.00", "EUR"), LocalDate.now(), cat, pub));
            bookId = book.getId();
            Member m = memberRepository.save(
                new Member("Cache Tester", "cache@test.com", LocalDate.now().plusYears(1)));
            memberId = m.getId();
            return null;
        });
    }

    @Test
    void secondReadServedFromCache() {
        // First call hits the database
        Book first = bookService.findById(bookId);
        // Second call should be served from cache (same object reference)
        Book second = bookService.findById(bookId);
        assertThat(second).isSameAs(first);
    }

    @Test
    void borrowEvictsCacheEntryAndNextReadHitsDatabase() {
        // Warm the cache
        bookService.findById(bookId);
        assertThat(cacheManager.getCache("books").get(bookId)).isNotNull();

        // Borrow evicts the cache entry
        loanService.borrow(memberId, bookId);
        assertThat(cacheManager.getCache("books").get(bookId)).isNull();

        // Next read hits the database and re-populates the cache
        Book afterBorrow = bookService.findById(bookId);
        assertThat(afterBorrow.getAvailableCopies()).isEqualTo(1);
    }
}