package com.learningfever.library.entity;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
    @SequenceGenerator(name = "author_seq", sequenceName = "author_seq", allocationSize = 1)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, length = 200)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    protected Author() {}

    public Author(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getBiography() { return biography; }
    public String getFullName() { return firstName + " " + lastName; }
    public Set<BookAuthor> getBookAuthors() { return Collections.unmodifiableSet(bookAuthors); }

    void addBookAuthorInternal(BookAuthor ba) { bookAuthors.add(ba); }
}