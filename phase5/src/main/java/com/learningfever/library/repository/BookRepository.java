package com.learningfever.library.repository;

import com.learningfever.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);

    Optional<Book> findByIsbn(String isbn);

    @Query("select b.id from Book b")
    Page<Long> findAllIds(Pageable pageable);

    @Query("select b from Book b join fetch b.bookAuthors ba join fetch ba.author where b.id in :ids")
    List<Book> findAllByIdsWithAuthors(@Param("ids") List<Long> ids);
}