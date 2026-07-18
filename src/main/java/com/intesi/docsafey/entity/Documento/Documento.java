package com.intesi.docsafey.entity.Documento;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import com.intesi.docsafey.entity.RichiestaCons.RichiestaConservazione;

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
   Long id;

   @Column(name = "name_file", nullable = false)
   String fileName;

   @Column(name = "mime_type", nullable = false)
   String mimeType;

   @Column(name = "file_size", nullable = false)
   Integer fileSize;

   @Column(name = "hash", nullable = false)
   String hash;

   @Column(name = "document_date", nullable = false)
   LocalDate documentDate;

   @ManyToOne(fetch = FetchType.LAZY)
   RichiestaConservazione request;
}
