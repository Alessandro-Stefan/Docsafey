package com.intesi.docsafey.dto.richiestaCons;

import java.util.List;

import com.intesi.docsafey.dto.documento.DocumentDto;
import com.intesi.docsafey.validation.annotation.UniqueHash;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@UniqueHash
public record AddRichiestaConsRequest(
    @NotNull
    Long externalId,

    @NotNull 
    Long producerId,

    @NotBlank 
    String documentType,

    @NotEmpty 
    List<@Valid DocumentDto> documents
) {}