package com.intesi.docsafey.exception.richiestaCons;

public class RichiestaAlreadyExistsException extends RuntimeException {
   public RichiestaAlreadyExistsException(Long producerId, Long externalId) {
    super(String.format("Richiesta di conservazione with producerID: %d and externalID: %d already exists",
      producerId, externalId));
   } 
}
