package com.learningfever.library.repository;

import com.learningfever.library.entity.Loan;
import com.learningfever.library.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("select count(l) from Loan l where l.member.id = :memberId and l.status in ('ACTIVE', 'OVERDUE')")
    long countActiveLoansForMember(@Param("memberId") Long memberId);

    @Query("select count(l) from Loan l where l.member.id = :memberId and l.book.id = :bookId and l.status in ('ACTIVE', 'OVERDUE')")
    long countActiveLoansByMemberAndBook(@Param("memberId") Long memberId, @Param("bookId") Long bookId);

    @Query("select l from Loan l join fetch l.book join fetch l.member where l.status = 'ACTIVE' and l.dueDate < :today")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);

    @Modifying
    @Query("update Loan l set l.status = 'OVERDUE' where l.status = 'ACTIVE' and l.dueDate < :today")
    int bulkMarkOverdue(@Param("today") LocalDate today);

    @Query("select l from Loan l join fetch l.book b join fetch l.member m where l.member.id = :memberId and l.status in ('ACTIVE', 'OVERDUE')")
    List<Loan> findActiveLoansForMember(@Param("memberId") Long memberId);
}