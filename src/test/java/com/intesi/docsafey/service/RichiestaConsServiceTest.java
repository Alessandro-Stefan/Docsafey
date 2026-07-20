package com.intesi.docsafey.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.intesi.docsafey.dto.documento.DocumentDto;
import com.intesi.docsafey.dto.richiestaCons.AddRichiestaConsRequest;
import com.intesi.docsafey.dto.richiestaCons.GeneralRichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.RichiestaStatusRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RichiestaConsServiceTest {
   
    @Mock
    private RichiestaConsRepository richiestaConsRepo;
    @Mock
    private RichiestaConsMapper richiestaConsMapper;
    @Mock
    private DocumentoRepository documentoRepo;
    @Mock
    private DocumentoMapper documentoMapper;

    @InjectMocks
    private RichiestaConsServiceImpl service;

    private AddRichiestaConsRequest buildValidRequest(Long producerId, Long externalId) {
        DocumentDto doc = new DocumentDto(null, "doc.pdf", "application/pdf", 20, "hash1", LocalDate.now());
        return new AddRichiestaConsRequest(externalId, producerId, "FATTURA", List.of(doc));
    }

    private RichiestaConservazione buildEntity(Long id, RichiestaStatus status) {
        RichiestaConservazione entity = new RichiestaConservazione();
        entity.setId(id);
        entity.setStatus(status);
        return entity;
    }

    // ==================================================
    // addRichiestaCons
    // ==================================================

    @Test
    void addRichiestaCons_201() {
        AddRichiestaConsRequest request = buildValidRequest(10L, 555L);
        RichiestaConservazione entity = buildEntity(100L, RichiestaStatus.RECEIVED);
        List<Documento> docs = List.of(new Documento());

        given(richiestaConsRepo.existsByProducerIdAndExternalId(10L, 555L)).willReturn(false);
        given(richiestaConsMapper.toEntity(request)).willReturn(entity);
        given(documentoMapper.toEntityList(request.documents(), entity)).willReturn(docs);

        Long id = service.addRichiestaCons(request);

        assertThat(id).isEqualTo(100L);
        verify(richiestaConsRepo).save(entity);
        verify(documentoRepo).saveAll(docs);
    }

    @Test
    void addRichiestaCons_409() {
        AddRichiestaConsRequest request = buildValidRequest(10L, 555L);

        given(richiestaConsRepo.existsByProducerIdAndExternalId(10L, 555L)).willReturn(true);

        assertThatThrownBy(() -> service.addRichiestaCons(request))
            .isInstanceOf(RichiestaAlreadyExistsException.class);

        verify(richiestaConsRepo, never()).save(any());
        verifyNoInteractions(documentoMapper, documentoRepo);
    }

    // ==================================================
    // getRichiestaCons
    // ==================================================

    @Test
    void getRichiestaCons_200() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.RECEIVED);
        RichiestaConsDto dto = new RichiestaConsDto(1L, 10L, 555L, "FATTURA", "RECEIVED", LocalDateTime.now().toString(), List.of());

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));
        given(richiestaConsMapper.toDto(entity)).willReturn(dto);

        RichiestaConsDto result = service.getRichiestaCons(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getRichiestaCons_404() {
        given(richiestaConsRepo.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRichiestaCons(99L))
            .isInstanceOf(RichiestaNotFoundException.class);

        verifyNoInteractions(richiestaConsMapper);
    }

    // ==================================================
    // searchRichiestaCons
    // ==================================================

    @Test
    void searchRichiestaCons_statusValid() {
        Pageable pageable = PageRequest.of(0, 10);
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.RECEIVED);
        Page<RichiestaConservazione> page = new PageImpl<>(List.of(entity), pageable, 1);
        GeneralRichiestaConsDto generalDto = new GeneralRichiestaConsDto(Long.valueOf(1), "FATTURA", "RECEIVED", LocalDate.now().toString());

        given(richiestaConsRepo.findAll(ArgumentMatchers.<Specification<RichiestaConservazione>>any(), eq(pageable)))
            .willReturn(page);
        given(richiestaConsMapper.toGeneralDto(entity)).willReturn(generalDto);

        SearchRichiestaConsResponse result = service.searchRichiestaCons(10L, "RECEIVED", pageable);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.richieste()).containsExactly(generalDto);
    }

    @Test
    void searchRichiestaCons_statusNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RichiestaConservazione> page = new PageImpl<>(List.of(), pageable, 0);

        given(richiestaConsRepo.findAll(ArgumentMatchers.<Specification<RichiestaConservazione>>any(), eq(pageable)))
            .willReturn(page);

        SearchRichiestaConsResponse result = service.searchRichiestaCons(10L, null, pageable);

        assertThat(result.totalElements()).isZero();
    }

    @Test
    void searchRichiestaCons_400() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> service.searchRichiestaCons(10L, "STATO_INESISTENTE", pageable))
            .isInstanceOf(RichiestaInvalidStatusException.class);

        verifyNoInteractions(richiestaConsRepo, richiestaConsMapper);
    }

    // ==================================================
    // validateRichiestaCons
    // ==================================================

    @Test
void validateRichiestaCons_200_validated() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.RECEIVED);

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));

        service.validateRichiestaCons(1L, new ValidateRichiestaConsRequest(RichiestaStatusRequest.VALIDATED));

        assertThat(entity.getStatus()).isEqualTo(RichiestaStatus.VALIDATED);
        assertThat(entity.getUpdatedAt()).isNotNull();
        verify(richiestaConsRepo).save(entity);
    }

    @Test
    void validateRichiestaCons_200_rejected() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.RECEIVED);

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));

        service.validateRichiestaCons(1L, new ValidateRichiestaConsRequest(RichiestaStatusRequest.REJECTED));

        assertThat(entity.getStatus()).isEqualTo(RichiestaStatus.REJECTED);
        verify(richiestaConsRepo).save(entity);
    }

    @Test
    void validateRichiestaCons_404() {
        given(richiestaConsRepo.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.validateRichiestaCons(1L, new ValidateRichiestaConsRequest(RichiestaStatusRequest.VALIDATED)))
            .isInstanceOf(RichiestaNotFoundException.class);

        verify(richiestaConsRepo, never()).save(any());
    }

    @Test
    void validateRichiestaCons_400() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.COMPLETED);

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.validateRichiestaCons(1L, new ValidateRichiestaConsRequest(RichiestaStatusRequest.VALIDATED)))
            .isInstanceOf(RichiestaInvalidStatusException.class);

        verify(richiestaConsRepo, never()).save(any());
    }

    // ==================================================
    // completeRichiestaCons
    // ==================================================

    @Test
    void completeRichiestaCons_200() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.VALIDATED);

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));

        service.completeRichiestaCons(1L);

        assertThat(entity.getStatus()).isEqualTo(RichiestaStatus.COMPLETED);
        assertThat(entity.getUpdatedAt()).isNotNull();
        verify(richiestaConsRepo).save(entity);
    }

    @Test
    void completeRichiestaCons_404() {
        given(richiestaConsRepo.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeRichiestaCons(1L))
            .isInstanceOf(RichiestaNotFoundException.class);

        verify(richiestaConsRepo, never()).save(any());
    }

    @Test
    void completeRichiestaCons_400() {
        RichiestaConservazione entity = buildEntity(1L, RichiestaStatus.RECEIVED);

        given(richiestaConsRepo.findById(1L)).willReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.completeRichiestaCons(1L))
            .isInstanceOf(RichiestaInvalidStatusException.class);

        verify(richiestaConsRepo, never()).save(any());
    }
}
