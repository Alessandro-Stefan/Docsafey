package com.intesi.docsafey.dto.richiestaCons;

import jakarta.validation.constraints.NotNull;

public record ValidateRichiestaConsRequest(
    @NotNull
    RichiestaStatusRequest status    
) {}
