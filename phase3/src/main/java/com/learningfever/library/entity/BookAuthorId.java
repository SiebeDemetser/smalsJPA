package com.learningfever.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// @EmbeddedId chosen over @IdClass because the key is a value object referenced by name in JPQL,
// making joins like "ba.id.bookId" explicit; @IdClass flattens the key into the entity fields
// which makes JPQL less clear for composite-key joins.
@Embeddable
public class BookAuthorId implements Serializable {

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "author_id")
    private Long authorId;

    protected BookAuthorId() {}

    public BookAuthorId(Long bookId, Long authorId) {
        this.bookId = bookId;
        this.authorId = authorId;
    }

    public Long getBookId() { return bookId; }
    public Long getAuthorId() { return authorId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookAuthorId that)) return false;
        return Objects.equals(bookId, that.bookId) && Objects.equals(authorId, that.authorId);
    }
    @Override public int hashCode() { return Objects.hash(bookId, authorId); }
}