package com.learningfever.library.service;

import com.learningfever.library.config.CacheConfig;
import com.learningfever.library.entity.*;
import com.learningfever.library.exception.*;
import com.learningfever.library.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private static final int MAX_ACTIVE_LOANS = 5;
    private static final int BORROW_DAYS = 14;
    private static final int MAX_RETRIES = 3;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository,
                       MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    // @CacheEvict on the outer method ensures cache eviction happens regardless of which attempt succeeds.
    // Evicting after borrow means the next read hits the database and sees the decremented availableCopies.
    @CacheEvict(value = CacheConfig.BOOKS_CACHE, key = "#bookId")
    public Loan borrow(Long memberId, Long bookId) {
        int attempts = 0;
        while (true) {
            try {
                return doBorrow(memberId, bookId);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRIES)
                    throw new OptimisticLockRetryExhaustedException(bookId);
            }
        }
    }

    @Transactional
    protected Loan doBorrow(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new DomainException("MEMBER_NOT_FOUND", "Member not found: " + memberId));

        if (!member.isActive())
            throw new MemberNotActiveException(memberId, member.getStatus().name());

        long activeCount = loanRepository.countActiveLoansForMember(memberId);
        if (activeCount >= MAX_ACTIVE_LOANS)
            throw new LoanLimitExceededException(memberId);

        long duplicateCount = loanRepository.countActiveLoansByMemberAndBook(memberId, bookId);
        if (duplicateCount > 0)
            throw new DuplicateActiveLoanException(memberId, bookId);

        Book book = bookRepository.findByIdWithLock(bookId)
            .orElseThrow(() -> new DomainException("BOOK_NOT_FOUND", "Book not found: " + bookId));

        if (!book.isAvailable())
            throw new NoCopiesAvailableException(bookId);

        book.decrementAvailableCopies();
        bookRepository.save(book);

        Loan loan = new Loan(member, book, LocalDate.now().plusDays(BORROW_DAYS));
        return loanRepository.save(loan);
    }

    @CacheEvict(value = CacheConfig.BOOKS_CACHE, key = "#result.book.id")
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new DomainException("LOAN_NOT_FOUND", "Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED)
            throw new LoanAlreadyReturnedException(loanId);

        loan.markReturned();

        Book book = loan.getBook();
        book.incrementAvailableCopies();
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    @Transactional
    public int markOverdueLoans() {
        int updated = loanRepository.bulkMarkOverdue(LocalDate.now());
        loanRepository.flush();
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Loan> findOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }
}