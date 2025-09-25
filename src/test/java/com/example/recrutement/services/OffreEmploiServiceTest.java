package com.example.recrutement.services;

import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.OffreEmploiRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreEmploiServiceTest {

    @Mock
    private OffreEmploiRepo repo;

    @InjectMocks
    private OffreEmploiService service;

    @Test
    void createOffreEmploi_savesEntity() {
        OffreEmploi offre = new OffreEmploi();
        service.createOffreEmploi(offre, null);
        verify(repo).save(offre);
    }

    @Test
    void updateOffreEmploi_updatesFieldsAndSaves() {
        OffreEmploi existing = new OffreEmploi();
        when(repo.findById(7)).thenReturn(Optional.of(existing));

        OffreEmploi update = new OffreEmploi();
        update.setTitre("New Title");
        update.setDescription("Desc");
        update.setArchive(true);
        update.setLocalisation("Tunis");
        update.setContrat("CDI");
        update.setNiveauExperience("3y");
        update.setSalaire(5000);

        service.updateOffreEmploi(7, update);

        ArgumentCaptor<OffreEmploi> captor = ArgumentCaptor.forClass(OffreEmploi.class);
        verify(repo).save(captor.capture());
        OffreEmploi saved = captor.getValue();
        assertEquals("New Title", saved.getTitre());
        assertEquals("Desc", saved.getDescription());
        assertTrue(saved.isArchive());
        assertEquals("Tunis", saved.getLocalisation());
        assertEquals("CDI", saved.getContrat());
        assertEquals("3y", saved.getNiveauExperience());
        assertEquals(5000, saved.getSalaire());
    }

    @Test
    void deleteOffreEmploi_deletesWhenExists() {
        OffreEmploi existing = new OffreEmploi();
        when(repo.findById(9)).thenReturn(Optional.of(existing));

        service.deleteOffreEmploi(9);

        verify(repo).deleteById(9);
    }
}


