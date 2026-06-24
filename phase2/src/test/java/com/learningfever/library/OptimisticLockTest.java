package com.learningfever.library;

import com.learningfever.library.entity.*;
import com.learningfever.library.repository.BookRepository;
import com.learningfever.library.repository.CategoryRepository;
import com.learningfever.library.repository.PublisherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockTest {

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Long bookId;

    @BeforeEach
    @Transactional
    void setUp() {
        Category category = categoryRepository.save(new Category("Fiction", "Fiction books"));
        Publisher publisher = publisherRepository.save(
                new Publisher("Test Publisher", "123 Main St", "info@testpub.com"));

        Book book = new Book(
                "978-0-00-000001-0",
                "Concurrency Test Book",
                1,
                Money.of("29.99", "EUR"),
                LocalDate.of(2024, 1, 1),
                category,
                publisher
        );
        book = bookRepository.save(book);
        bookId = book.getId();
    }

    @Test
    void secondConcurrentDecrementThrowsOptimisticLockException() {
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        Book bookInEm1 = em1.find(Book.class, bookId);

        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Book bookInEm2 = em2.find(Book.class, bookId);

        bookInEm1.decrementAvailableCopies();
        em1.getTransaction().commit();
        em1.close();

        bookInEm2.decrementAvailableCopies();

        assertThatThrownBy(() -> {
            em2.getTransaction().commit();
        })
            .isInstanceOf(Exception.class)
            .satisfies(ex -> {
                Throwable cause = ex;
                boolean foundOptimisticLock = false;
                while (cause != null) {
                    if (cause instanceof org.hibernate.StaleObjectStateException
                            || cause instanceof jakarta.persistence.OptimisticLockException
                            || cause instanceof ObjectOptimisticLockingFailureException) {
                        foundOptimisticLock = true;
                        break;
                    }
                    cause = cause.getCause();
                }
                if (!foundOptimisticLock) {
                    throw new AssertionError(
                        "Expected an optimistic locking exception in the cause chain, but got: "
                        + ex.getClass().getName() + ": " + ex.getMessage(), ex);
                }
            });

        if (em2.getTransaction().isActive()) {
            em2.getTransaction().rollback();
        }
        em2.close();
    }
}