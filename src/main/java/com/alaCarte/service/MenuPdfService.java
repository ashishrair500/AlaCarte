package com.alaCarte.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class MenuPdfService {

    private final ResourceLoader resourceLoader;
    private String cachedMenuText;

    @Value("${menu.pdf.path:classpath:menu.pdf}")
    private String menuPath;

    public MenuPdfService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getMenuText() {
        if (cachedMenuText != null) {
            return cachedMenuText;
        }

        try {
            Resource resource = resourceLoader.getResource(menuPath);
            byte[] pdfBytes = resource.getInputStream().readAllBytes();
            
            try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                cachedMenuText = stripper.getText(document);
                return cachedMenuText;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read menu PDF: " + e.getMessage(), e);
        }
    }

    public void clearCache() {
        cachedMenuText = null;
    }
}

