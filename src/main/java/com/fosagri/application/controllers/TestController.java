package com.fosagri.application.controllers;

import com.fosagri.application.utils.TestDataCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestDataCreator testDataCreator;

    @GetMapping("/create-prestation")
    public String createTestPrestation() {
        try {
            testDataCreator.createTestPrestation();
            return "✅ Prestation de test créée avec succès !";
        } catch (Exception e) {
            return "❌ Erreur: " + e.getMessage();
        }
    }
}