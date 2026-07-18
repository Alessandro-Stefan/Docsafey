package com.intesi.docsafey.dto.RichiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.Documento.DocumentDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddRichiestaConsRequest(
    @NotNull Long external,
    @NotNull Long producerId,
    @NotBlank String documentType,
    @NotNull List<DocumentDto> documents
) {}