package com.intesi.docsafey.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.GeneralRichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.SearchRichiestaConsResponse;
import com.intesi.docsafey.dto.richiestaCons.ValidateRichiestaConsRequest;
import com.intesi.docsafey.entity.documento.Documento;
import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;
import com.intesi.docsafey.entity.richiestaCons.RichiestaStatus;
import com.intesi.docsafey.exception.richiestaCons.RichiestaAlreadyExistsException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaInvalidStatusException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaNotFoundException;
import com.intesi.docsafey.mapper.DocumentoMapper;
import com.intesi.docsafey.mapper.RichiestaConsMapper;
import com.intesi.docsafey.repository.DocumentoRepository;
import com.intesi.docsafey.repository.RichiestaConsRepository;
import com.intesi.docsafey.repository.specification.RichiestaConsSpecifications;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RichiestaConsServiceImpl implements RichiestaConsService{

    private final RichiestaConsRepository richiestaConsRepo;
    private final RichiestaConsMapper richiestaConsMapper;

    private final DocumentoRepository documentoRepo;
    private final DocumentoMapper documentoMapper;

    public RichiestaConsServiceImpl(RichiestaConsRepository richiestaConsRepo, RichiestaConsMapper richiestaConsMapper, DocumentoRepository documentoRepo, DocumentoMapper documentoMapper) {
        this.richiestaConsRepo = richiestaConsRepo;
        this.richiestaConsMapper = richiestaConsMapper;
        this.documentoRepo = documentoRepo;
        this.documentoMapper = documentoMapper;
    }
    
    @Override
    @Transactional
    public Long addRichiestaCons(AddRichiestaConsRequest request) {
        boolean exists = richiestaConsRepo.existsByProducerIdAndExternalId(request.producerId(), request.externalId());

        if (exists) 
            throw new RichiestaAlreadyExistsException(request.producerId(), request.externalId());

        log.info("Creating richiesta di conservazione with producerID={} externalID={} numDocs={}",
            request.producerId(), request.externalId(), request.documents().size());

        RichiestaConservazione entity = richiestaConsMapper.toEntity(request);
        richiestaConsRepo.save(entity);

        List<Documento> docs = documentoMapper.toEntityList(request.documents(), entity);
        documentoRepo.saveAll(docs);

        log.info("Richiesta di conservazione created ID={}", entity.getId());

        return entity.getId();
    }

    @Override
    public RichiestaConsDto getRichiestaCons(Long id) {
        log.debug("Retrieving richiesta di conservazione with ID={}", id);
        Optional<RichiestaConservazione> entity = richiestaConsRepo.findById(id);

        if (entity.isEmpty())  
           throw new RichiestaNotFoundException(id); 
    
        RichiestaConsDto res = richiestaConsMapper.toDto(entity.get());
        return res;
    }

    @Override
    public SearchRichiestaConsResponse searchRichiestaCons(Long producerId, String status, Pageable pageable) {
        log.debug("Searching richieste di conservazione with producerID={} status={} page={} size={}",
            producerId, status, pageable.getPageNumber(), pageable.getPageSize());

        RichiestaStatus statusQuery = null;

        if (status != null) {
            try {
            statusQuery = RichiestaStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid status filter provided: '{}'", status);
                throw new RichiestaInvalidStatusException(status);
            }
        }

        Specification<RichiestaConservazione> spec = Specification.allOf(
            RichiestaConsSpecifications .hasProducerId(producerId),
            RichiestaConsSpecifications.hasStatus(statusQuery)
        );

        Page<RichiestaConservazione> page = richiestaConsRepo.findAll(spec, pageable);

        List<GeneralRichiestaConsDto> richieste = page.getContent()
                                                        .stream()
                                                        .map(richiestaConsMapper::toGeneralDto)
                                                        .toList();

        SearchRichiestaConsResponse res = new SearchRichiestaConsResponse(
            richieste,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );

        return res;
    }

   @Override
   @Transactional
    public void validateRichiestaCons(Long id, ValidateRichiestaConsRequest request) {
        log.info("Validating richiesta di conservazione with ID={}", id);

        Optional<RichiestaConservazione> entityCheck = richiestaConsRepo.findById(id);

        if(entityCheck.isEmpty())  
            throw new RichiestaNotFoundException(id);

        RichiestaConservazione entity = entityCheck.get();        

        if (!entity.getStatus().equals(RichiestaStatus.RECEIVED)) {
           throw new RichiestaInvalidStatusException(String.format("Richiesta di conservazione with ID: %d has an invalid status, status required: %s | found: %s", 
                                                        entity.getId(), 
                                                        RichiestaStatus.RECEIVED.name(), 
                                                        entity.getStatus().name()));
        }

        RichiestaStatus newStatus = RichiestaStatus.valueOf(request.status().name());
        entity.setStatus(newStatus);
        entity.setUpdatedAt(LocalDateTime.now());

        richiestaConsRepo.save(entity);

        log.info("Richiesta di conservazione with ID={} validated, new status={}", entity.getId(), newStatus.toString());
    }

    @Override
    @Transactional
    public void completeRichiestaCons(Long id) {
        log.info("Completing richiesta di conservazione with ID={}", id);

        Optional<RichiestaConservazione> entityCheck = richiestaConsRepo.findById(id);

        if(entityCheck.isEmpty())
            throw new RichiestaNotFoundException(id);

        RichiestaConservazione entity = entityCheck.get();        

        if (!entity.getStatus().equals(RichiestaStatus.VALIDATED)) {
           throw new RichiestaInvalidStatusException(String.format("Richiesta di conservazione with ID: %d has an invalid status, status required: %s | found: %s", 
                                                        entity.getId(), 
                                                        RichiestaStatus.VALIDATED.name(), 
                                                        entity.getStatus().name()));
        }

        entity.setStatus(RichiestaStatus.COMPLETED);
        entity.setUpdatedAt(LocalDateTime.now());
    
        richiestaConsRepo.save(entity);

        log.info("Richiesta di conservazione with ID={} completed", id);
    }

}
