package com.intesi.docsafey.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.intesi.docsafey.dto.documento.DocumentDto;
import com.intesi.docsafey.entity.documento.Documento;
import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;

@Component
public class DocumentoMapper {

    public Documento toEntity(DocumentDto dto, RichiestaConservazione entityRic) {
        Documento entity = new Documento();
        entity.setFileName(dto.filename());
        entity.setMimeType(dto.mimeType());
        entity.setFileSize(dto.fileSize());
        entity.setHash(dto.hash());
        entity.setDocumentDate(dto.documentDate());
        entity.setRequest(entityRic);

        return entity;
    }

    public DocumentDto toDto(Documento entity) {
        DocumentDto dto = new DocumentDto(
        entity.getId(),
        entity.getFileName(),
        entity.getMimeType(),
        entity.getFileSize(), 
        entity.getHash(), 
        entity.getDocumentDate());

        return dto;
    }

    public List<Documento> toEntityList(List<DocumentDto> dtos, RichiestaConservazione entityRic) {
        List<Documento> list = new ArrayList<>();

        for (DocumentDto dto : dtos) {
           Documento entity = new Documento();
           entity = toEntity(dto, entityRic);

           list.add(entity);
        }

        return list;
    }

    public List<DocumentDto> toDtoList(List<Documento> entities) {
        List<DocumentDto> list = new ArrayList<>();

        for (Documento entity : entities) {
            list.add(toDto(entity));
        }

        return list;
    }
    
}
