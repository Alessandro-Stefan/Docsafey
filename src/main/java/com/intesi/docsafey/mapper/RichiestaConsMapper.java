package com.intesi.docsafey.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.GeneralRichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;
import com.intesi.docsafey.entity.richiestaCons.RichiestaStatus;

@Component
public class RichiestaConsMapper {

    private final DocumentoMapper docMapper;

    public RichiestaConsMapper(DocumentoMapper docMapper) {
        this.docMapper = docMapper;
    }
    
    public RichiestaConservazione toEntity(AddRichiestaConsRequest dto) {
        RichiestaConservazione entity = new RichiestaConservazione();
        entity.setProducerId(dto.producerId());
        entity.setExternalId(dto.externalId());
        entity.setStatus(RichiestaStatus.RECEIVED);
        entity.setDocumentType(dto.documentType());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDocuments(docMapper.toEntityList(dto.documents(), entity));

        return entity;
    }

    public RichiestaConsDto toDto(RichiestaConservazione entity) {
        RichiestaConsDto dto = new RichiestaConsDto(
            entity.getId(),
            entity.getProducerId(),
            entity.getExternalId(),
            entity.getDocumentType(),
            entity.getStatus().toString(),
            entity.getCreatedAt().toString(),
            docMapper.toDtoList(entity.getDocuments()));

        return dto;
    }

    public GeneralRichiestaConsDto toGeneralDto(RichiestaConservazione entity){
        GeneralRichiestaConsDto dto = new GeneralRichiestaConsDto(
            entity.getId(),
            entity.getDocumentType(),
            entity.getStatus().toString(),
            entity.getCreatedAt().toString()
        );
    
        return dto;
    }
}
