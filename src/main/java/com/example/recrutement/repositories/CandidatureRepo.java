package com.example.recrutement.repositories;

import com.example.recrutement.entities.Candidature;

import com.example.recrutement.entities.Challenge;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidatureRepo extends JpaRepository<Candidature, Integer>, JpaSpecificationExecutor<Candidature> {


  @Query("""
    SELECT c FROM Candidature c
    JOIN FETCH c.offreEmploi
     """)

    List<Candidature> findAllCandidature();
    @EntityGraph(attributePaths = {"offreEmploi"})
    Page<Candidature> findAll(Pageable pageable);

    List<Candidature> findByDefi(Challenge defi);

    @Query("SELECT c FROM Candidature c")
    Page<Candidature> findAllSimple(Pageable pageable);

    @EntityGraph(attributePaths = {"offreEmploi"})
    @Query("SELECT c FROM Candidature c WHERE c.creePar = :userId")
    List<Candidature> findByCreePar(@Param("userId") String userId);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM candidature WHERE id = :id", nativeQuery = true)
  void deleteByIdNative(@Param("id") Integer id);


}
