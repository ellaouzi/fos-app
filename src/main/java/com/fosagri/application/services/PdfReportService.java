package com.fosagri.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.model.AdhAgent;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfReportService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public byte[] generateDemandeReport(DemandePrestation demande) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        
        // Add header and footer event handlers to place them at top and bottom of each page
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderEventHandler());
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());
        
        Document document = new Document(pdf);
        
        // Set margins to accommodate header and footer
        document.setMargins(100, 36, 80, 36);

        // Add title (prestation label)
        String prestationLabel = demande.getPrestation() != null ? 
            demande.getPrestation().getLabel() : "DEMANDE DE PRESTATION";
        Paragraph title = new Paragraph(prestationLabel)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginTop(5)
                .setMarginBottom(10);
        document.add(title);

        // Add agent and prestation info sections
        addAgentAndPrestationInfo(document, demande);

        // Add separator line between info and content sections
        addInfoContentSeparator(document);

        // Add form responses if available
        if (demande.getReponseJson() != null && !demande.getReponseJson().trim().isEmpty()) {
            addFormResponses(document, demande.getReponseJson());
        }

        // Add comments if available
        if (demande.getCommentaire() != null && !demande.getCommentaire().trim().isEmpty()) {
            addComments(document, demande.getCommentaire());
        }

        document.close();
        return baos.toByteArray();
    }

    // Header event handler class to place header at top of each page
    private class HeaderEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas canvas = new PdfCanvas(page);
            
            try {
                // Try to load header image
                ClassPathResource headerResource = new ClassPathResource("META-INF/resources/images/header.png");
                if (headerResource.exists()) {
                    try (InputStream headerStream = headerResource.getInputStream()) {
                        byte[] headerBytes = headerStream.readAllBytes();
                        ImageData headerImageData = ImageDataFactory.create(headerBytes);
                        
                        // Calculate header position (top of page)
                        float headerHeight = 80;
                        float headerY = pageSize.getTop() - headerHeight - 10;
                        float headerX = pageSize.getLeft() + 36; // Left margin
                        float headerWidth = pageSize.getWidth() - 72; // Page width minus left and right margins
                        
                        // Add header image
                        canvas.addImageFittedIntoRectangle(headerImageData, 
                            new Rectangle(headerX, headerY, headerWidth, headerHeight), false);
                    }
                } else {
                    // If header image fails to load, add text header
                    canvas.beginText()
                          .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 16)
                          .moveText(pageSize.getWidth() / 2 - 150, pageSize.getTop() - 50)
                          .showText("FOS AGRI - Système de Gestion des Demandes")
                          .endText();
                }
            } catch (Exception e) {
                // Fallback text header
                try {
                    canvas.beginText()
                          .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 16)
                          .moveText(pageSize.getWidth() / 2 - 150, pageSize.getTop() - 50)
                          .showText("FOS AGRI - Système de Gestion des Demandes")
                          .endText();
                } catch (Exception ex) {
                    // Ignore if even text header fails
                }
            }
        }
    }

    // Footer event handler class to place footer at bottom of each page
    private class FooterEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas canvas = new PdfCanvas(page);
            
            try {
                // Try to load footer image
                ClassPathResource footerResource = new ClassPathResource("META-INF/resources/images/footer.png");
                if (footerResource.exists()) {
                    try (InputStream footerStream = footerResource.getInputStream()) {
                        byte[] footerBytes = footerStream.readAllBytes();
                        ImageData footerImageData = ImageDataFactory.create(footerBytes);
                        
                        // Calculate footer position (bottom of page)
                        float footerHeight = 60;
                        float footerY = pageSize.getBottom() + 10;
                        float footerX = pageSize.getLeft() + 36; // Left margin
                        float footerWidth = pageSize.getWidth() - 72; // Page width minus left and right margins
                        
                        // Add footer image
                        canvas.addImageFittedIntoRectangle(footerImageData, 
                            new Rectangle(footerX, footerY, footerWidth, footerHeight), false);
                    }
                } else {
                    // If footer image fails to load, add text footer
                    canvas.beginText()
                          .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 10)
                          .moveText(pageSize.getWidth() / 2 - 100, pageSize.getBottom() + 30)
                          .showText("FOS AGRI - Rapport généré le " + dateFormat.format(new java.util.Date()))
                          .endText();
                }
            } catch (Exception e) {
                // Fallback text footer
                try {
                    canvas.beginText()
                          .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 10)
                          .moveText(pageSize.getWidth() / 2 - 100, pageSize.getBottom() + 30)
                          .showText("FOS AGRI - Rapport généré le " + dateFormat.format(new java.util.Date()))
                          .endText();
                } catch (Exception ex) {
                    // Ignore if even text footer fails
                }
            }
        }
    }

    private void addAgentAndPrestationInfo(Document document, DemandePrestation demande) {
        // Create main container table with 2 columns
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(8)
                .setMarginBottom(0);
        
        // Left column - Agent Info
        Cell agentCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10);
        
        // Remove AGENT INFO title
        
        if (demande.getAgent() != null) {
            String agentName = formatAgentName(demande.getAgent());
            addInfoLine(agentCell, "Nom", agentName);
            addInfoLine(agentCell, "CIN", demande.getAgent().getCIN_AG() != null ? demande.getAgent().getCIN_AG() : "N/A");
            addInfoLine(agentCell, "ID Adhérent", demande.getAgent().getIdAdh() != null ? demande.getAgent().getIdAdh() : "N/A");
        } else {
            addInfoLine(agentCell, "Agent", "N/A");
        }
        
        // Right column - Prestation Info
        Cell prestationCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10);
        
        // Remove PRESTATION INFO title and Service line
        
        addInfoLine(prestationCell, "N° Demande", demande.getId() != null ? demande.getId().toString() : "N/A");
        addInfoLine(prestationCell, "Statut", getStatutLabel(demande.getStatut()));
        addInfoLine(prestationCell, "Date de Demande", 
            demande.getDateDemande() != null ? dateFormat.format(demande.getDateDemande()) : "N/A");
        
        mainTable.addCell(agentCell);
        mainTable.addCell(prestationCell);
        document.add(mainTable);
        
        // Remove dates section
    }
    
    private void addInfoLine(Cell cell, String label, String value) {
        // Create paragraph with key and value separated by tab
        Paragraph infoPara = new Paragraph()
                .setMarginTop(2)
                .setMarginBottom(2);
        
        // Add label in bold with colon
        infoPara.add(new com.itextpdf.layout.element.Text(label + ":")
                .setBold()
                .setFontSize(9));
        
        // Add tab character for spacing
        infoPara.add(new com.itextpdf.layout.element.Text("\t")
                .setFontSize(9));
        
        // Add value after tab
        infoPara.add(new com.itextpdf.layout.element.Text(value != null ? value : "N/A")
                .setFontSize(9));
        
        cell.add(infoPara);
    }
    
    
    private String getStatutLabel(String statut) {
        if (statut == null || statut.isEmpty()) return "N/A";
        switch (statut) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }

    private void addFormResponses(Document document, String reponseJson) {
        try {
            Map<String, Object> responses = objectMapper.readValue(reponseJson, new TypeReference<Map<String, Object>>() {});
            
            if (!responses.isEmpty()) {
                // Separator line is now added in main method
                
                // Add responses as simple text paragraphs
                for (Map.Entry<String, Object> entry : responses.entrySet()) {
                    String formattedValue = formatResponseValue(entry.getValue());
                    String displayValue = formattedValue != null && !formattedValue.trim().isEmpty() ? formattedValue : "Non renseigné";
                    
                    // Format key with first letter uppercase
                    String formattedKey = capitalizeFirstLetter(entry.getKey());
                    
                    // Create paragraph in format "Key: Value"
                    Paragraph responsePara = new Paragraph()
                            .setMarginTop(3)
                            .setMarginBottom(1);
                    
                    // Add key in bold with colon
                    responsePara.add(new com.itextpdf.layout.element.Text(formattedKey + ":")
                            .setBold()
                            .setFontSize(10));
                    
                    // Add tab character for spacing
                    responsePara.add(new com.itextpdf.layout.element.Text("\t")
                            .setFontSize(10));
                    
                    // Add value in normal text
                    com.itextpdf.layout.element.Text valueText = new com.itextpdf.layout.element.Text(displayValue)
                            .setFontSize(10);
                    
                    // Style empty values
                    if (formattedValue == null || formattedValue.trim().isEmpty() || "N/A".equals(formattedValue)) {
                        valueText.setItalic().setFontColor(ColorConstants.GRAY);
                    }
                    
                    responsePara.add(valueText);
                    document.add(responsePara);
                }
                
                // Add minimal space after responses
                document.add(new Paragraph().setMargin(5));
            }
        } catch (JsonProcessingException e) {
            // If JSON parsing fails, add compact note
            Paragraph errorNote = new Paragraph("Données du formulaire non analysables")
                    .setFontSize(9)
                    .setItalic()
                    .setMarginTop(15)
                    .setMarginBottom(5);
            document.add(errorNote);
        }
    }

    private void addComments(Document document, String commentaire) {
        Paragraph commentsTitle = new Paragraph("Commentaires")
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(8);
        document.add(commentsTitle);

        Paragraph commentsText = new Paragraph(commentaire)
                .setMarginBottom(10);
        document.add(commentsText);
    }

    private Cell createHeaderCell(String content) {
        return new Cell()
                .add(new Paragraph(content).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "N/A")));
    }
    
    private String formatAgentName(AdhAgent agent) {
        if (agent == null) {
            return "N/A";
        }
        
        // Format: NOM_AG PR_AG (ID: idAdh)
        String nom = agent.getNOM_AG() != null ? agent.getNOM_AG() : "";
        String prenom = agent.getPR_AG() != null ? agent.getPR_AG() : "";
        String id = agent.getIdAdh() != null ? agent.getIdAdh() : "N/A";
        
        if (!nom.isEmpty() || !prenom.isEmpty()) {
            return nom + " " + prenom + " (ID: " + id + ")";
        }
        
        return "Agent inconnu (ID: " + id + ")";
    }
    
    private String formatResponseValue(Object value) {
        if (value == null) {
            return "N/A";
        }
        
        String valueStr = value.toString();
        
        // Check if the value contains enfant/conjoint format: {id=3, nom=BENALI, prenom=Hassan}
        Pattern enfantConjointPattern = Pattern.compile("\\{id=(\\d+),\\s*nom=([^,]+),\\s*prenom=([^}]+)\\}");
        Matcher matcher = enfantConjointPattern.matcher(valueStr);
        
        if (matcher.find()) {
            String nom = matcher.group(2);
            String prenom = matcher.group(3);
            return nom + " " + prenom;
        }
        
        // Check for other common patterns like lists or objects
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            // Handle arrays/lists - remove brackets and format nicely
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        return valueStr;
    }
    
    private void addInfoContentSeparator(Document document) {
        // Create separator line with dashes (exactly 12px from info section)
        Paragraph separatorPara = new Paragraph("-----------------------------------------")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(12)
                .setMarginBottom(8);
        
        document.add(separatorPara);
    }
    
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}