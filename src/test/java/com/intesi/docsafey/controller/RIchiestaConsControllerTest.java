package com.intesi.docsafey.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.intesi.docsafey.dto.richiestaCons.RichiestaConsDto;
import com.intesi.docsafey.dto.richiestaCons.SearchRichiestaConsResponse;
import com.intesi.docsafey.exception.richiestaCons.RichiestaAlreadyExistsException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaInvalidStatusException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaNotFoundException;
import com.intesi.docsafey.service.RichiestaConsService;

@WebMvcTest(RichiestaConsController.class)
@AutoConfigureMockMvc(addFilters = false)
class RichiestaConsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RichiestaConsService service;

    // ---------- Helper ----------

    private String validAddRequestJson() {
        return """
            {
              "externalId": 555,
              "producerId": 10,
              "documentType": "FATTURA",
              "documents": [
                {
                  "filename": "doc1.pdf",
                  "mimeType": "application/pdf",
                  "fileSize": 1024,
                  "hash": "hash1",
                  "documentDate": "%s"
                }
              ]
            }
            """.formatted(LocalDate.now());
    }

    // ==================================================
    // GET /{id}
    // ==================================================

    @Test
    void getRichiestaConsById_200() throws Exception {
        RichiestaConsDto dto = new RichiestaConsDto(1L, 10L, 555L, "FATTURA", "RECEIVED", LocalDate.now().toString(), List.of());
        given(service.getRichiestaCons(1L)).willReturn(dto);

        mockMvc.perform(get("/v1/conservazione/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getRichiestaConsById_404() throws Exception {
        given(service.getRichiestaCons(99L)).willThrow(new RichiestaNotFoundException(99L));

        mockMvc.perform(get("/v1/conservazione/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Richiesta not found"));
    }

    // ==================================================
    // GET (search)
    // ==================================================

    @Test
    void searchRichiesteCons_statusValido_200() throws Exception {
        SearchRichiestaConsResponse res = new SearchRichiestaConsResponse(List.of(), 0, 20, 0, 0);
        given(service.searchRichiestaCons(eq(10L), eq("RECEIVED"), any())).willReturn(res);

        mockMvc.perform(get("/v1/conservazione")
                .param("producerId", "10")
                .param("status", "RECEIVED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void searchRichiesteCons_statusNonValido_400() throws Exception {
        given(service.searchRichiestaCons(eq(10L), eq("BOH"), any()))
            .willThrow(new RichiestaInvalidStatusException("BOH"));

        mockMvc.perform(get("/v1/conservazione")
                .param("producerId", "10")
                .param("status", "BOH"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Status Richiesta invalid"));
    }

    // ==================================================
    // POST
    // ==================================================

    @Test
    void addRichiestaCons_201() throws Exception {
        given(service.addRichiestaCons(any())).willReturn(100L);

        mockMvc.perform(post("/v1/conservazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validAddRequestJson()))
            .andExpect(status().isCreated())
            .andExpect(content().string("100"));
    }

    @Test
    void addRichiestaCons_producerIdMancante_400() throws Exception {
        String invalidJson = """
            {
              "externalId": 555,
              "documentType": "FATTURA",
              "documents": [
                {"filename": "doc1.pdf", "mimeType": "application/pdf", "fileSize": 1024, "hash": "h1", "documentDate": "%s"}
              ]
            }
            """.formatted(LocalDate.now());

        mockMvc.perform(post("/v1/conservazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid request payload"));

        verifyNoInteractions(service);
    }

    @Test
    void addRichiestaCons_documentsVuoto_400() throws Exception {
        String invalidJson = """
            {
              "externalId": 555,
              "producerId": 10,
              "documentType": "FATTURA",
              "documents": []
            }
            """;

        mockMvc.perform(post("/v1/conservazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void addRichiestaCons_documentDateFutura_400() throws Exception {
        String invalidJson = """
            {
              "externalId": 555,
              "producerId": 10,
              "documentType": "FATTURA",
              "documents": [
                {"filename": "doc1.pdf", "mimeType": "application/pdf", "fileSize": 1024, "hash": "h1", "documentDate": "%s"}
              ]
            }
            """.formatted(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/conservazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addRichiestaCons_400PerUniqueHash() throws Exception {
        String invalidJson = """
            {
              "externalId": 555,
              "producerId": 10,
              "documentType": "FATTURA",
              "documents": [
                {"filename": "doc1.pdf", "mimeType": "application/pdf", "fileSize": 1024, "hash": "SAME", "documentDate": "%s"},
                {"filename": "doc2.pdf", "mimeType": "application/pdf", "fileSize": 2048, "hash": "SAME", "documentDate": "%s"}
              ]
            }
            """.formatted(LocalDate.now(), LocalDate.now());

        mockMvc.perform(post("/v1/conservazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    // ==================================================
    // PUT /validate/{id}
    // ==================================================

    @Test
    void validateRichiestaCons_200() throws Exception {
        mockMvc.perform(put("/v1/conservazione/validate/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "VALIDATED"}
                    """))
            .andExpect(status().isNoContent());

        verify(service).validateRichiestaCons(eq(1L), any());
    }

    @Test
    void validateRichiestaCons_404() throws Exception {
        doThrow(new RichiestaNotFoundException(1L))
            .when(service).validateRichiestaCons(eq(1L), any());

        mockMvc.perform(put("/v1/conservazione/validate/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "VALIDATED"}
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void validateRichiestaCons_400() throws Exception {
        doThrow(new RichiestaInvalidStatusException("stato errato"))
            .when(service).validateRichiestaCons(eq(1L), any());

        mockMvc.perform(put("/v1/conservazione/validate/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "VALIDATED"}
                    """))
            .andExpect(status().isBadRequest());
    }

    // ==================================================
    // PUT complete/{id}
    // ==================================================

    @Test
    void completeRichiestaCons_200() throws Exception {
        mockMvc.perform(put("/v1/conservazione/complete/1"))
            .andExpect(status().isNoContent());

        verify(service).completeRichiestaCons(1L);
    }

    @Test
    void completeRichiestaCons_404() throws Exception {
        doThrow(new RichiestaNotFoundException(1L))
            .when(service).completeRichiestaCons(1L);

        mockMvc.perform(put("/v1/conservazione/complete/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void completeRichiestaCons_400() throws Exception {
        doThrow(new RichiestaInvalidStatusException("stato errato"))
            .when(service).completeRichiestaCons(1L);

        mockMvc.perform(put("/v1/conservazione/complete/1"))
            .andExpect(status().isBadRequest());
    }
}
