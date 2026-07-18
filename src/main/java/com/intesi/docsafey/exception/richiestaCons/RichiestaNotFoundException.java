package com.intesi.docsafey.exception.richiestaCons;

public class RichiestaNotFoundException extends RuntimeException{
   public RichiestaNotFoundException(Long id) {
    super(String.format("Richiesta di conservazione with ID: %d not found", id));
   } 
}
