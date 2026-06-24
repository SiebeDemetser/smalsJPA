package com.learningfever.library.exception;

public class NoCopiesAvailableException extends DomainException {

    public NoCopiesAvailableException(Long bookId) {
        super("NO_COPIES_AVAILABLE",
            "No available copies for book " + bookId);
    }
}