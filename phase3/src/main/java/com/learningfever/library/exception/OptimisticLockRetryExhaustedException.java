package com.learningfever.library.exception;

public class OptimisticLockRetryExhaustedException extends DomainException {
    public OptimisticLockRetryExhaustedException(Long bookId) {
        super("OPTIMISTIC_LOCK_RETRY_EXHAUSTED",
            "Could not borrow book " + bookId + " after maximum retry attempts due to concurrent access");
    }
}