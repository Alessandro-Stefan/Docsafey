package com.intesi.docsafey.service;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.ValidateRichiestaConsRequest;

public interface RichiestaConsService {
   Long addRichiestaCons(AddRichiestaConsRequest request); 

   RichiestaConsDto getRichiestaCons(Long id);

   void validateRichiestaCons(Long id, ValidateRichiestaConsRequest request);;

   void completeRichiestaCons(Long id);
}
