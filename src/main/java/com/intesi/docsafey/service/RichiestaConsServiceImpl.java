package com.intesi.docsafey.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
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
        Optional<RichiestaConservazione> entityCheck = richiestaConsRepo.findByProducerIdAndExternalId(request.producerId(), request.externalId());

        if (entityCheck.isPresent())
            throw new RichiestaAlreadyExistsException(request.producerId(), request.externalId());

        RichiestaConservazione entity = richiestaConsMapper.toEntity(request);
        List<Documento> docs = documentoMapper.toEntityList(request.documents(), entity);

        documentoRepo.saveAll(docs);
        richiestaConsRepo.save(entity);

        return entity.getId();
    }

    @Override
    public RichiestaConsDto getRichiestaCons(Long id) {
        Optional<RichiestaConservazione> entity = richiestaConsRepo.findById(id);

        if (entity.isEmpty()) 
           throw new RichiestaNotFoundException(id); 
    
        RichiestaConsDto res = richiestaConsMapper.toDto(entity.get());
        return res;
    }

   @Override
   @Transactional
    public void validateRichiestaCons(Long id, ValidateRichiestaConsRequest request) {
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
    }

    @Override
    @Transactional
    public void completeRichiestaCons(Long id) {
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
    }

}
