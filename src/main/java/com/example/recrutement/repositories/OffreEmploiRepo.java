package com.example.recrutement.repositories;

import com.example.recrutement.entities.OffreEmploi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OffreEmploiRepo extends JpaRepository<OffreEmploi, Integer>, JpaSpecificationExecutor<OffreEmploi> {
   // @Query("""
          //  SELECT offre
          //  FROM OffreEmploi offre
           // WHERE offre.archive = false""")
  //  Page<OffreEmploi> findAllOffres(Pageable pageable, String userId);
   @Query("""
    SELECT offre
    FROM OffreEmploi offre
""")
   List<OffreEmploi> findAllOffres();

    @Query("SELECT o FROM OffreEmploi o WHERE " +
            "(LOWER(o.titre) LIKE %:keyword% OR " +
            "o.description LIKE %:keyword% OR " +  // no LOWER on description
            "LOWER(o.contrat) LIKE %:keyword% OR " +
            "LOWER(o.niveauExperience) LIKE %:keyword% OR " +
            "CAST(o.salaire AS string) LIKE %:keyword% OR " +
            "LOWER(o.localisation) LIKE %:keyword%) " +
            "AND o.archive = false")
    List<OffreEmploi> searchByKeyword(@Param("keyword") String keyword);


    @Query("SELECT AVG(o.salaire) FROM OffreEmploi o")
    Double getAverageSalary();

}
