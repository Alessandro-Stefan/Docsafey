package com.intesi.docsafey.dto.richiestaCons;

import java.util.List;

public record SearchRichiestaConsResponse(
    List<GeneralRichiestaConsDto> richieste,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {}
