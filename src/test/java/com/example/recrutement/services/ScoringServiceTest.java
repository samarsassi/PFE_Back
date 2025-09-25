package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private CandidatureRepo candidatureRepo;

    @Mock
    private OffreEmploiRepo offreEmploiRepo;

    @Mock
    private OllamaService ollamaService;

    @InjectMocks
    private ScoringService scoringService;

    private Candidature candidature;
    private OffreEmploi offre;

    @BeforeEach
    void setUp() {
        candidature = new Candidature();
        candidature.setCvUrl("http://example.com/cv.pdf");
        candidature.setId(1);

        offre = new OffreEmploi();
        offre.setTitre("Data Analyst");
    }

    @Test
    void buildOptimizedPrompt_containsAllSections() {
        String prompt = scoringService.buildOptimizedPrompt(
                "Data Analyst",
                "Analyze data and build dashboards",
                "Candidate CV text"
        );

        assertTrue(prompt.contains("You are an expert recruiter"));
        assertTrue(prompt.contains("Title:"));
        assertTrue(prompt.contains("Description:"));
        assertTrue(prompt.contains("--- CANDIDATE CV ---"));
        assertTrue(prompt.contains("Data Analyst"));
        assertTrue(prompt.contains("Analyze data"));
        assertTrue(prompt.contains("Candidate CV text"));
    }

    @Test
    void scoreCandidature_withEmptyJobDescription_setsErrorAndSaves() throws Exception {
        // Arrange
        offre.setDescription("");

        // Extract CV text should not throw and should be called, but we won't reach AI call
        when(ollamaService.extractTextFromPdf(anyString())).thenReturn("Some CV text");

        // Act
        scoringService.scoreCandidature(candidature, offre);

        // Assert save called with error state
        ArgumentCaptor<Candidature> captor = ArgumentCaptor.forClass(Candidature.class);
        verify(candidatureRepo, atLeastOnce()).save(captor.capture());
        Candidature saved = captor.getValue();
        assertEquals(0.0, saved.getScoreCV());
        assertNotNull(saved.getScoringComment());
        assertTrue(saved.getScoringComment().toLowerCase().contains("missing"));
    }
}


