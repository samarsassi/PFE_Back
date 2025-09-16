package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.OffreEmploiRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OffreEmploiService implements IOffreEmploiService{

    private final OffreEmploiRepo offreEmploiRepository;

    @Autowired
    public OffreEmploiService(OffreEmploiRepo offreEmploiRepository) {
        this.offreEmploiRepository = offreEmploiRepository;
    }

    // Create a new OffreEmploi
    @Transactional
    public OffreEmploi createOffreEmploi(OffreEmploi offreEmploi, Authentication connectedUser) {

        return offreEmploiRepository.save(offreEmploi);
    }
    // Get all offres d'emploi
    public List<OffreEmploi> getAllOffresEmploi() {
        return offreEmploiRepository.findAllOffres();
    }

    // Get an offre emploi by id
    public Optional<OffreEmploi> getOffreEmploiById(Integer id) {
        return offreEmploiRepository.findById(id);
    }

    // Update an existing offre emploi
    @Transactional
    public OffreEmploi updateOffreEmploi(Integer id, OffreEmploi updatedOffreEmploi) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            // Update properties
            offreEmploi.setModifiePar(updatedOffreEmploi.getModifiePar());
            offreEmploi.setDateModification(updatedOffreEmploi.getDateModification());
            offreEmploi.setTitre(updatedOffreEmploi.getTitre());
            offreEmploi.setDescription(updatedOffreEmploi.getDescription());
            offreEmploi.setArchive(updatedOffreEmploi.isArchive());
            offreEmploi.setLocalisation(updatedOffreEmploi.getLocalisation());
            offreEmploi.setDateDebut(updatedOffreEmploi.getDateDebut());
            offreEmploi.setContrat(updatedOffreEmploi.getContrat());
            offreEmploi.setCategories(updatedOffreEmploi.getCategories());
            offreEmploi.setNiveauExperience(updatedOffreEmploi.getNiveauExperience());
            offreEmploi.setExigences(updatedOffreEmploi.getExigences());
            offreEmploi.setSalaire(updatedOffreEmploi.getSalaire());

            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Delete an offre emploi by id
    @Transactional
    public void deleteOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            offreEmploiRepository.deleteById(id);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Archive an offre emploi (set archive flag to true)
    @Transactional
    public OffreEmploi archiveOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            offreEmploi.setArchive(true);
            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Unarchive an offre emploi (set archive flag to false)
    @Transactional
    public OffreEmploi unarchiveOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            offreEmploi.setArchive(false);
            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Get all candidatures related to a specific offre emploi
    public List<Candidature> getCandidaturesForOffreEmploi(Integer id) {
        Optional<OffreEmploi> offreEmploi = offreEmploiRepository.findById(id);
        if (offreEmploi.isPresent()) {
            return offreEmploi.get().getCandidatures();
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    public List<OffreEmploi> searchOffers(String keyword) {
        return offreEmploiRepository.searchByKeyword(keyword.toLowerCase());
    }

    @PersistenceContext
    private EntityManager entityManager;

    public List<OffreEmploi> searchByKeywords(List<String> keywords) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OffreEmploi> query = cb.createQuery(OffreEmploi.class);
        Root<OffreEmploi> root = query.from(OffreEmploi.class);

        List<Predicate> predicates = new ArrayList<>();

        for (String keyword : keywords) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            predicates.add(cb.like(cb.lower(root.get("titre")), likeKeyword));
            // Don't use lower() on description if it's a CLOB
            predicates.add(cb.like(root.get("description"), likeKeyword));
            predicates.add(cb.like(cb.lower(root.get("contrat")), likeKeyword));
            predicates.add(cb.like(cb.lower(root.get("salaire")), likeKeyword));
            predicates.add(cb.like(cb.lower(root.get("niveauExperience")), likeKeyword));
            predicates.add(cb.like(cb.lower(root.get("localisation")), likeKeyword));
            // For salaire, if numeric, handle differently or skip
        }


        query.select(root).where(cb.or(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }

    //stats
    public long countOffers() {
        return offreEmploiRepository.count();
    }

      public double getAverageSalary() {
        // Implement query to calculate average salary from DB offers
       return offreEmploiRepository.getAverageSalary();
     }

}
