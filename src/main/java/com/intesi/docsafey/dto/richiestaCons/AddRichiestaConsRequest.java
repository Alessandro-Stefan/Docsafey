package com.intesi.docsafey.dto.richiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.documento.DocumentDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddRichiestaConsRequest(
    @NotNull Long external,
    @NotNull Long producerId,
    @NotBlank String documentType,
    @NotNull List<DocumentDto> documents
) {}