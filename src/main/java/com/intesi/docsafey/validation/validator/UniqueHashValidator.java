package com.intesi.docsafey.validation.validator;

import java.util.ArrayList;
import java.util.List;

import com.intesi.docsafey.dto.documento.DocumentDto;
import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.validation.annotation.UniqueHash;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueHashValidator implements ConstraintValidator<UniqueHash, AddRichiestaConsRequest> {
    
    @Override
    public boolean isValid(AddRichiestaConsRequest request, ConstraintValidatorContext context) {
        List<String> hashes = new ArrayList<>();

        if (request.documents() == null) 
           return true;

        for (DocumentDto documento : request.documents()) {
            if (hashes.contains(documento.hash())) 
                return false;
            
            hashes.add(documento.hash());
        }

        return true;
    }
}
