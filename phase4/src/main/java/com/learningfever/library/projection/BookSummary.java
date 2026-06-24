package com.learningfever.library.projection;

public interface BookSummary {
    Long getId();
    String getTitle();
    int getAvailableCopies();
    String getPrimaryAuthorName(); // resolved via JPQL constructor expression
}