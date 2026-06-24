package com.learningfever.library.dto;

public record BookSummaryDto(Long id, String title, String primaryAuthorName, int availableCopies) {
}