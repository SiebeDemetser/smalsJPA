package com.learningfever.library.service;

import com.learningfever.library.dto.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsService {

    private final EntityManager em;

    public AnalyticsService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public List<CategoryCountDto> booksPerCategory() {
        return em.createQuery(
            "select new com.learningfever.library.dto.CategoryCountDto(c.name, count(b)) " +
            "from Book b join b.category c " +
            "group by c.name order by count(b) desc",
            CategoryCountDto.class
        ).getResultList();
    }

    @Transactional(readOnly = true)
    public List<BorrowCountDto> busiestMembers() {
        return em.createQuery(
            "select new com.learningfever.library.dto.BorrowCountDto(m.id, m.name, count(l)) " +
            "from Loan l join l.member m " +
            "where l.status in ('ACTIVE', 'OVERDUE') " +
            "group by m.id, m.name order by count(l) desc",
            BorrowCountDto.class
        ).getResultList();
    }

    @Transactional(readOnly = true)
    public List<TitleBorrowCountDto> mostBorrowedTitles(LocalDate from, LocalDate to) {
        return em.createQuery(
            "select new com.learningfever.library.dto.TitleBorrowCountDto(b.id, b.title, count(l)) " +
            "from Loan l join l.book b " +
            "where l.borrowDate >= :from and l.borrowDate <= :to " +
            "group by b.id, b.title order by count(l) desc",
            TitleBorrowCountDto.class
        ).setParameter("from", from).setParameter("to", to).getResultList();
    }

    @Transactional(readOnly = true)
    public AvgLoanDurationDto averageLoanDuration() {
        // FUNCTION('DATEDIFF', ...) is not portable; use JPQL date arithmetic instead.
        // For PostgreSQL: (returnDate - borrowDate) via a native query.
        // For H2: same SQL works. We use a native query and mark it non-portable explicitly.
        Number result = (Number) em.createNativeQuery(
            "SELECT AVG(EXTRACT(EPOCH FROM (return_date - borrow_date)) / 86400) " +
            "FROM loans WHERE status = 'RETURNED' AND return_date IS NOT NULL"
        ).getSingleResult();
        double avg = result != null ? result.doubleValue() : 0.0;
        return new AvgLoanDurationDto(avg);
    }
}