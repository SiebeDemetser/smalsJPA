package com.learningfever.library.dto;

public record BorrowCountDto(Long memberId, String memberName, long activeLoanCount) {}