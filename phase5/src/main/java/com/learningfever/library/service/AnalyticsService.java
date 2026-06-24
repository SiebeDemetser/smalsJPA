package com.learningfever.library.service;

import com.learningfever.library.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final EntityManager em;

    public AnalyticsService(EntityManager em) {
        this.em = em;
    }

    // Dashboard summary may be up to 60 seconds stale — acceptable for a statistics page.
    @Cacheable(CacheConfig.LIBRARY_SUMMARY_CACHE)
    @Transactional(readOnly = true)
    public Map<String, Object> librarySummary() {
        long totalBooks = (Long) em.createQuery("select count(b) from Book b").getSingleResult();
        long activeLoans = (Long) em.createQuery(
            "select count(l) from Loan l where l.status in ('ACTIVE','OVERDUE')").getSingleResult();
        long totalMembers = (Long) em.createQuery("select count(m) from Member m").getSingleResult();
        return Map.of("totalBooks", totalBooks, "activeLoans", activeLoans, "totalMembers", totalMembers);
    }

    @Transactional(readOnly = true)
    public List<?> booksPerCategory() {
        return em.createQuery(
            "select c.name, count(b) from Book b join b.category c group by c.name order by count(b) desc"
        ).getResultList();
    }
}