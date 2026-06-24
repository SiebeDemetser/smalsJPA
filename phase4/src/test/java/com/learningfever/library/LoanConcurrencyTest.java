package com.learningfever.library;

import com.learningfever.library.entity.*;
import com.learningfever.library.exception.NoCopiesAvailableException;
import com.learningfever.library.exception.OptimisticLockRetryExhaustedException;
import com.learningfever.library.repository.*;
import com.learningfever.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LoanConcurrencyTest {

    @Autowired LoanService loanService;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired PublisherRepository publisherRepository;
    @Autowired TransactionTemplate transactionTemplate;

    private Long bookId;
    private List<Long> memberIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            Category cat = categoryRepository.save(new Category("Fiction", "Fiction books"));
            Publisher pub = publisherRepository.save(new Publisher("Test Pub", "Addr", "pub@test.com"));
            Book book = bookRepository.save(new Book(
                "978-TEST-002", "Concurrent Book P4", 1,
                Money.of("9.99", "EUR"), LocalDate.now(), cat, pub));
            bookId = book.getId();

            for (int i = 0; i < 10; i++) {
                Member m = memberRepository.save(new Member(
                    "Member " + i, "memberp4" + i + "@test.com",
                    LocalDate.now().plusYears(1)));
                memberIds.add(m.getId());
            }
            return null;
        });
    }

    @Test
    void onlyOneBorrowSucceedsForLastCopy() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final Long memberId = memberIds.get(i);
            futures.add(executor.submit(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                try {
                    loanService.borrow(memberId, bookId);
                    successes.incrementAndGet();
                } catch (NoCopiesAvailableException | OptimisticLockRetryExhaustedException e) {
                    failures.incrementAndGet();
                }
            }));
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Book book = bookRepository.findById(bookId).orElseThrow();
        assertThat(successes.get()).isEqualTo(1);
        assertThat(failures.get()).isEqualTo(9);
        assertThat(book.getAvailableCopies()).isEqualTo(0);
        assertThat(book.getAvailableCopies()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void returnRestoresAvailableCount() {
        Loan loan = loanService.borrow(memberIds.get(0), bookId);
        assertThat(bookRepository.findById(bookId).orElseThrow().getAvailableCopies()).isEqualTo(0);

        loanService.returnBook(loan.getId());
        assertThat(bookRepository.findById(bookId).orElseThrow().getAvailableCopies()).isEqualTo(1);
    }
}