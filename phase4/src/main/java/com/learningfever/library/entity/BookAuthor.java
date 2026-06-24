package com.learningfever.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_authors")
public class BookAuthor {

    @EmbeddedId
    private BookAuthorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    @JoinColumn(name = "author_id")
    private Author author;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_role", nullable = false, length = 20)
    private AuthorRole role;

    protected BookAuthor() {}

    BookAuthor(Book book, Author author, AuthorRole role) {
        this.id = new BookAuthorId(book.getId(), author.getId());
        this.book = book;
        this.author = author;
        this.role = role;
    }

    public BookAuthorId getId() { return id; }
    public Book getBook() { return book; }
    public Author getAuthor() { return author; }
    public AuthorRole getRole() { return role; }
}