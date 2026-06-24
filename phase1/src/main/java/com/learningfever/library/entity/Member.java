package com.learningfever.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "membership_expiry", nullable = false)
    private LocalDate membershipExpiry;

    @Column(name = "suspended_until")
    private LocalDate suspendedUntil;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<Loan> loans = new HashSet<>();

    protected Member() {}

    public Member(String name, String email, LocalDate membershipExpiry) {
        this.name = name;
        this.email = email;
        this.membershipExpiry = membershipExpiry;
    }

    public MemberStatus getStatus() {
        LocalDate today = LocalDate.now();
        if (suspendedUntil != null && !today.isAfter(suspendedUntil)) {
            return MemberStatus.SUSPENDED;
        }
        if (today.isAfter(membershipExpiry)) {
            return MemberStatus.EXPIRED;
        }
        return MemberStatus.ACTIVE;
    }

    public boolean isActive() { return getStatus() == MemberStatus.ACTIVE; }

    public void suspend(LocalDate until) {
        if (until == null || until.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Suspension date must be in the future");
        }
        this.suspendedUntil = until;
    }

    public void reinstate() { this.suspendedUntil = null; }

    public void renewMembership(LocalDate newExpiry) {
        if (newExpiry == null || newExpiry.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("New expiry must be in the future");
        }
        this.membershipExpiry = newExpiry;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDate getMembershipExpiry() { return membershipExpiry; }
    public LocalDate getSuspendedUntil() { return suspendedUntil; }

    public Set<Loan> getLoans() { return Collections.unmodifiableSet(loans); }

    void addLoanInternal(Loan loan) { loans.add(loan); }
}