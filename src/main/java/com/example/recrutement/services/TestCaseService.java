package com.example.recrutement.services;

import com.example.recrutement.entities.TestCase;
import com.example.recrutement.repositories.TestCaseRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestCaseService {


    private final TestCaseRepo testCaseRepository;

    public TestCaseService(TestCaseRepo testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    public List<TestCase> findAll() {
        return testCaseRepository.findAll();
    }

    public Optional<TestCase> findById(Integer id) {
        return testCaseRepository.findById(id);
    }

    public TestCase save(TestCase testCase) {
        return testCaseRepository.save(testCase);
    }

    public void deleteById(Integer id) {
        testCaseRepository.deleteById(id);
    }
}
