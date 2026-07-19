package com.intesi.docsafey.entity.documento;

import java.time.LocalDate;

import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "documenti")
public class Documento {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY) 
   private Long id;

   @Column(name = "name_file", nullable = false)
   private String fileName;

   @Column(name = "mime_type", nullable = false)
   private String mimeType;

   @Column(name = "file_size", nullable = false)
   private Integer fileSize;

   @Column(name = "hash", nullable = false)
   private String hash;

   @Column(name = "document_date", nullable = false)
   private LocalDate documentDate;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "request_id")
   private RichiestaConservazione request;
}
