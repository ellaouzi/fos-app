package com.fosagri.application.controllers;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private DemandePrestationService demandeService;

    @GetMapping("/demande/{id}")
    public ResponseEntity<byte[]> generateDemandeReport(@PathVariable Long id) {
        try {
            DemandePrestation demande = demandeService.findById(id);
            if (demande == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = pdfReportService.generateDemandeReport(demande);

            String filename = String.format("demande_%d_%s.pdf", 
                id, 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/demande/{id}/download")
    public ResponseEntity<byte[]> downloadDemandeReport(@PathVariable Long id) {
        try {
            DemandePrestation demande = demandeService.findById(id);
            if (demande == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = pdfReportService.generateDemandeReport(demande);

            String filename = String.format("demande_%d_%s.pdf", 
                id, 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}