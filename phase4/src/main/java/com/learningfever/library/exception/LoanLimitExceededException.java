package com.learningfever.library.exception;

public class LoanLimitExceededException extends DomainException {
    public LoanLimitExceededException(Long memberId) {
        super("LOAN_LIMIT_EXCEEDED", "Member " + memberId + " has reached the maximum of 5 active loans");
    }
}