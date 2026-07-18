package com.intesi.docsafey.entity.RichiestaCons;

import java.time.LocalDateTime;
import java.util.List;

import com.intesi.docsafey.entity.Documento.Documento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "richieste_conservazione",
         uniqueConstraints = @UniqueConstraint(columnNames = {"producer_id", "external_id"})
)
public class RichiestaConservazione {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id; 

   @Column(name = "external_id", nullable = false)
   Long externalId;

   @Column(name = "producer_id", nullable = false)
   Long producerId;

   @Column(name = "document_type", nullable = false)
   String documentType;
   
   @Enumerated(EnumType.STRING)
   @Column(name = "status", nullable = false)
   RichiestaStatus status;

   @Column(name = "created_at", nullable = false)
   LocalDateTime createdAt;

   @Column(name = "updated_at", nullable = true)
   LocalDateTime updatedAt;

   @OneToMany(mappedBy = "request")
   List<Documento> documents;
}
