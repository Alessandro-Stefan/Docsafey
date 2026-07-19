package com.intesi.docsafey.service;

import org.springframework.data.domain.Pageable;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.SearchRichiestaConsResponse;
import com.intesi.docsafey.dto.richiestaCons.ValidateRichiestaConsRequest;

public interface RichiestaConsService {
   Long addRichiestaCons(AddRichiestaConsRequest request); 

   RichiestaConsDto getRichiestaCons(Long id);

   SearchRichiestaConsResponse searchRichiestaCons(Long producerId, String status, Pageable pageable);

   void validateRichiestaCons(Long id, ValidateRichiestaConsRequest request);

   void completeRichiestaCons(Long id);
}
