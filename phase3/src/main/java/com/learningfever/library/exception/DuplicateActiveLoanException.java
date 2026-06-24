package com.learningfever.library.exception;

public class DuplicateActiveLoanException extends DomainException {
    public DuplicateActiveLoanException(Long memberId, Long bookId) {
        super("DUPLICATE_ACTIVE_LOAN", "Member " + memberId + " already has an active loan for book " + bookId);
    }
}