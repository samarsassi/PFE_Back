package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatureServiceTest {

    @Mock
    private CandidatureRepo candidatureRepo;

    @Mock
    private OffreEmploiRepo offreEmploiRepo;

    @Mock
    private SoumissionDefiRepo soumissionDefiRepo;

    @InjectMocks
    private CandidatureService candidatureService;

    private Candidature candidature;

    @BeforeEach
    void setUp() {
        candidature = new Candidature();
        candidature.setId(100);
    }

    @Test
    void updateExpiredDefiIfNeeded_expiresWhenOver48Hours() {
        candidature.setStatutDefi(Candidature.StatutDefi.ENVOYE);
        candidature.setDefiEnvoyeLe(LocalDateTime.now().minusHours(49));

        candidatureService.updateExpiredDefiIfNeeded(candidature);

        ArgumentCaptor<Candidature> captor = ArgumentCaptor.forClass(Candidature.class);
        verify(candidatureRepo).save(captor.capture());
        Candidature saved = captor.getValue();
        assertEquals(Candidature.StatutDefi.EXPIRE, saved.getStatutDefi());
        assertNotNull(saved.getDefiTermineLe());
    }

    @Test
    void createCandidature_linksOfferAndSaves() {
        OffreEmploi offre = new OffreEmploi();
        when(offreEmploiRepo.findById(5)).thenReturn(Optional.of(offre));

        Candidature input = new Candidature();
        candidatureService.createCandidature(input, 5, null);

        ArgumentCaptor<Candidature> captor = ArgumentCaptor.forClass(Candidature.class);
        verify(candidatureRepo).save(captor.capture());
        assertSame(offre, captor.getValue().getOffreEmploi());
    }
}


