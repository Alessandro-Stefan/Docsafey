package com.intesi.docsafey.dto.richiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.documento.DocumentDto;

public record GetRichiestaConsResponse (
    Long id,
    Long externalId,
    String documentType,
    String status,
    String createdAt,
    List<DocumentDto> documents
){}
