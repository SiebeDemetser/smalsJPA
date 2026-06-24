package com.learningfever.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "loan_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    protected Loan() {}

    public Loan(Member member, Book book, LocalDate dueDate) {
        this.member = member;
        this.book = book;
        this.status = LoanStatus.REQUESTED;
        this.borrowDate = LocalDate.now();
        this.dueDate = dueDate;
    }

    public void activate() {
        if (status != LoanStatus.REQUESTED)
            throw new IllegalStateException("Only REQUESTED loans can be activated, current: " + status);
        this.status = LoanStatus.ACTIVE;
    }

    public void markOverdue() {
        if (status != LoanStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE loans can become OVERDUE, current: " + status);
        this.status = LoanStatus.OVERDUE;
    }

    public void markReturned() {
        if (status == LoanStatus.RETURNED)
            throw new IllegalStateException("Loan is already returned");
        if (status != LoanStatus.ACTIVE && status != LoanStatus.OVERDUE)
            throw new IllegalStateException("Cannot return a loan in status: " + status);
        this.status = LoanStatus.RETURNED;
        this.returnDate = LocalDate.now();
    }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LoanStatus getStatus() { return status; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
}