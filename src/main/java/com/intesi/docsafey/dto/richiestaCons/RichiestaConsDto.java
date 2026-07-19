package com.intesi.docsafey.dto.richiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.documento.DocumentDto;

public record RichiestaConsDto(
    Long id,
    Long producerId,
    Long externalId,
    String documentType,
    String status,
    String createdAt,
    List<DocumentDto> documents
){}
