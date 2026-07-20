package com.intesi.docsafey.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.SearchRichiestaConsResponse;
import com.intesi.docsafey.dto.richiestaCons.ValidateRichiestaConsRequest;
import com.intesi.docsafey.service.RichiestaConsService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/v1/conservazione")
public class RichiestaConsController {

     private final RichiestaConsService service;

     public RichiestaConsController(RichiestaConsService service) {
          this.service = service;
     }

     @GetMapping("/{id}")
     public ResponseEntity<RichiestaConsDto> getRichiestaConsById(@PathVariable Long id) {
          RichiestaConsDto res = service.getRichiestaCons(id);
          return ResponseEntity.ok(res);
     }

     @GetMapping()
     public ResponseEntity<SearchRichiestaConsResponse> searchRichiesteCons(
          @RequestParam(required = false) Long producerId,
          @RequestParam(required = false) String status,
          @PageableDefault(size = 20) Pageable pageable) {

          SearchRichiestaConsResponse res = service.searchRichiestaCons(producerId, status, pageable);
          return ResponseEntity.ok(res);
     }

     @PostMapping()
     public ResponseEntity<Long> addRichiestaCons(@RequestBody @Valid AddRichiestaConsRequest request) {
          Long id = service.addRichiestaCons(request);
          URI location = URI.create("/" + id);
          return ResponseEntity.created(location).body(id);
     }

     @PutMapping("/validate/{id}")
     public ResponseEntity<Void> validateRichiestaCons(
          @PathVariable Long id,
          @RequestBody @Valid ValidateRichiestaConsRequest request) {

          service.validateRichiestaCons(id, request);
          return ResponseEntity.noContent().build();
     }

     @PutMapping("/complete/{id}")
     public ResponseEntity<Void> completeRichiestaCons(@PathVariable Long id) {
          service.completeRichiestaCons(id);
          return ResponseEntity.noContent().build();
     }
}
