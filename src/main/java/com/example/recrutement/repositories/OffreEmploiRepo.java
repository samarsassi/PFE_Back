package com.example.recrutement.repositories;

import com.example.recrutement.entities.OffreEmploi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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

}
