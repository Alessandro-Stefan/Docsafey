package com.intesi.docsafey.dto.documento;

import java.time.LocalDate;

import com.intesi.docsafey.validation.annotation.UniqueHash;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;

public record DocumentDto(

    @Null
    Long id,

    @NotBlank 
    String filename,

    @NotBlank 
    String mimeType,

    @Min(1) 
    Integer fileSize,

    @NotBlank 
    String hash,

    @NotNull 
    @PastOrPresent
    LocalDate documentDate
) {}
