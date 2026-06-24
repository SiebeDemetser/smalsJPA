package com.learningfever.library.exception;

public class MemberNotActiveException extends DomainException {
    public MemberNotActiveException(Long memberId, String status) {
        super("MEMBER_NOT_ACTIVE", "Member " + memberId + " cannot borrow: status is " + status);
    }
}