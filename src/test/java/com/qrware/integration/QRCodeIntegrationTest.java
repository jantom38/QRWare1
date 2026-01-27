package com.qrware.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.qr.QRCodeType;
import com.qrware.repository.qr.QRCodeDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QRCodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QRCodeDataRepository qrCodeRepository;

    @BeforeEach
    void clean() {
        // keep transactional - ensure clean slate inside test
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"QR_GENERATE"})
    void generateQRCode_Manual_ShouldCreateRecord() throws Exception {
        String code = "QR-INT-" + System.currentTimeMillis();

        String body = """
            {
              "code": "%s",
              "type": "PRODUCT",
              "entityType": "product",
              "entityId": 123,
              "data": "test-data",
              "active": true,
              "format": "PNG",
              "size": 250
            }
            """.formatted(code);

        mockMvc.perform(post("/api/qr-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.type").value("PRODUCT"))
            .andExpect(jsonPath("$.entityType").value("product"))
            .andExpect(jsonPath("$.entityId").value(123));

        assertTrue(qrCodeRepository.findByCode(code).isPresent());
    }

    @Test
    @WithMockUser(username = "worker", authorities = {"QR_SCAN"})
    void scanQRCode_ShouldIncrementScanCount() throws Exception {
        String code = "QR-SCAN-" + System.currentTimeMillis();

        QRCodeData qr = new QRCodeData();
        qr.setCode(code);
        qr.setType(QRCodeType.PRODUCT);
        qr.setEntityType("product");
        qr.setEntityId(123L);
        qr.setData("data");
        qr.setActive(true);
        qr.setScanCount(0L);
        qr.setCreatedAt(LocalDateTime.now());
        qr = qrCodeRepository.save(qr);

        mockMvc.perform(get("/api/qr-codes/code/{code}", code)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code));

        QRCodeData updated = qrCodeRepository.findById(qr.getId()).orElseThrow();
        assertEquals(1L, updated.getScanCount());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"QR_GENERATE"})
    void generateQRCode_Manual_ShouldFail_WhenDuplicateCode() throws Exception {
        String code = "QR-DUP-" + System.currentTimeMillis();

        QRCodeData existing = new QRCodeData();
        existing.setCode(code);
        existing.setType(QRCodeType.PRODUCT);
        existing.setEntityType("product");
        existing.setEntityId(1L);
        existing.setActive(true);
        existing.setScanCount(0L);
        existing.setCreatedAt(LocalDateTime.now());
        qrCodeRepository.save(existing);

        String body = """
            {
              "code": "%s",
              "type": "PRODUCT",
              "entityType": "product",
              "entityId": 999,
              "data": "dup"
            }
            """.formatted(code);

        mockMvc.perform(post("/api/qr-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(anyOf(
                containsStringIgnoringCase("duplicate"),
                containsStringIgnoringCase("already exists"),
                containsStringIgnoringCase("już istnieje"),
                containsStringIgnoringCase("conflict"),
                containsStringIgnoringCase("konflikt")
            )));
    }

    @Test
    void generateQRCode_ShouldReturn401_WhenNoAuth() throws Exception {
        String body = """
            {
              "type": "PRODUCT",
              "entityType": "product",
              "entityId": 1,
              "data": "data"
            }
            """;

        mockMvc.perform(post("/api/qr-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }
}
