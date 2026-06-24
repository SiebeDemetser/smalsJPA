package com.learningfever.library.repository;

import com.learningfever.library.entity.Book;
import com.learningfever.library.projection.BookSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);

    Optional<Book> findByIsbn(String isbn);

    @Query("select b.id from Book b")
    Page<Long> findAllIds(Pageable pageable);

    @Query("select b from Book b join fetch b.bookAuthors ba join fetch ba.author where b.id in :ids")
    List<Book> findAllByIdsWithAuthors(@Param("ids") List<Long> ids);

    // Interface projection — Hibernate selects only the projected columns (id, title, available_copies)
    // plus any joined columns. Constructor projection (new BookSummaryDto(...)) always executes the
    // full SELECT and instantiates via reflection; an interface projection lets Hibernate generate
    // a narrower SELECT when the interface fields map directly to entity properties.
    @Query("select b.id as id, b.title as title, b.availableCopies as availableCopies from Book b where b.id in :ids")
    List<BookSummary> findSummariesByIds(@Param("ids") List<Long> ids);
}