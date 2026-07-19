package com.intesi.docsafey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.intesi.docsafey.entity.documento.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, Long>{
    
}
