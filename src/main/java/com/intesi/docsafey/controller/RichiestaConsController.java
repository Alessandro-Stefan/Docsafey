package com.intesi.docsafey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.intesi.docsafey.dto.RichiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.RichiestaCons.GetRichiestaConsResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/v1/conservazione")
public class RichiestaConsController {
   
    @GetMapping("/{id}")
    public ResponseEntity<GetRichiestaConsResponse> getRichiestaConsById(@PathVariable Long id) {
        GetRichiestaConsResponse res = null;
        return ResponseEntity.ok(res);
    }

   @PostMapping()
   public ResponseEntity<Long> addRichiestaCons(@RequestBody @Valid AddRichiestaConsRequest request) {
        Long id = null;
        return ResponseEntity.ok(id);
   }

   @PutMapping("/{id}")
   public ResponseEntity<Void> validateRichiestaCons(@PathVariable Long id) {
        return null;
   }
}
