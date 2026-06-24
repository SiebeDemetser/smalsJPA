package com.learningfever.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_seq")
    @SequenceGenerator(name = "book_seq", sequenceName = "book_seq", allocationSize = 1)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Embedded
    private Money price;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private Set<Loan> loans = new HashSet<>();

    protected Book() {}

    public Book(String isbn, String title, int totalCopies, Money price,
                LocalDate publicationDate, Category category, Publisher publisher) {
        this.isbn = isbn;
        this.title = title;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.price = price;
        this.publicationDate = publicationDate;
        this.category = category;
        this.publisher = publisher;
    }

    public void addAuthor(Author author, AuthorRole role) {
        BookAuthor bookAuthor = new BookAuthor(this, author, role);
        bookAuthors.add(bookAuthor);
        author.addBookAuthorInternal(bookAuthor);
    }

    public void removeAuthor(Author author) {
        bookAuthors.removeIf(ba -> {
            if (ba.getAuthor().equals(author)) {
                author.removeBookAuthorInternal(ba);
                return true;
            }
            return false;
        });
    }

    public boolean isAvailable() { return availableCopies > 0; }

    void decrementAvailableCopies() {
        if (availableCopies <= 0) {
            throw new IllegalStateException("No available copies for book: " + isbn);
        }
        this.availableCopies--;
    }

    void incrementAvailableCopies() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                "Available copies cannot exceed total copies for book: " + isbn);
        }
        this.availableCopies++;
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public Money getPrice() { return price; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public Category getCategory() { return category; }
    public Publisher getPublisher() { return publisher; }

    public Set<BookAuthor> getBookAuthors() { return Collections.unmodifiableSet(bookAuthors); }
    public Set<Loan> getLoans() { return Collections.unmodifiableSet(loans); }
}