package com.intesi.docsafey.dto.richiestaCons;

public record GeneralRichiestaConsDto(
    Long id,
    String documentType,
    String status,
    String createdAt
) {}
