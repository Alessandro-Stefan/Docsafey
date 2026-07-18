package com.intesi.docsafey.dto.documento;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentDto(
    @NotBlank String filename,
    @NotBlank String mimeType,
    Integer fileSize,
    @NotBlank String hash,
    @NotNull LocalDate documentDate
) {}
