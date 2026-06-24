package com.learningfever.library.exception;

public class LoanAlreadyReturnedException extends DomainException {
    public LoanAlreadyReturnedException(Long loanId) {
        super("LOAN_ALREADY_RETURNED", "Loan " + loanId + " has already been returned");
    }
}