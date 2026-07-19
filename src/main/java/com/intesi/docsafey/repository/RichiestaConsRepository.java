package com.intesi.docsafey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;

public interface RichiestaConsRepository extends JpaRepository<RichiestaConservazione, Long> {
   Optional<RichiestaConservazione> findByProducerIdAndExternalId(Long producerId, Long externalId);; 
}
