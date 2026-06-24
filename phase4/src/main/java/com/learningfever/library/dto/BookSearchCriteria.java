package com.learningfever.library.dto;

import com.learningfever.library.entity.Money;
import java.time.LocalDate;

public record BookSearchCriteria(
    String titleLike,
    String authorNameLike,
    Long categoryId,
    Money priceCeiling,
    Boolean availableOnly,
    LocalDate publishedAfter,
    LocalDate publishedBefore
) {}