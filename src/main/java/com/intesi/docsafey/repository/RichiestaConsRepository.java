package com.intesi.docsafey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;

public interface RichiestaConsRepository extends JpaRepository<RichiestaConservazione, Long>, JpaSpecificationExecutor<RichiestaConservazione> {
   boolean existsByProducerIdAndExternalId(Long producerId, Long externalId);; 
}
