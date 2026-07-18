package com.intesi.docsafey.dto.RichiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.Documento.DocumentDto;

public record GetRichiestaConsResponse (
    Long id,
    Long externalId,
    String documentType,
    String status,
    String createdAt,
    List<DocumentDto> documents
){}
