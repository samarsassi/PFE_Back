package com.example.recrutement.repositories;

import com.example.recrutement.entities.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepo extends JpaRepository<TestCase, Integer> {
}
