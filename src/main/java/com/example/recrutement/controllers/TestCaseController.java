package com.example.recrutement.controllers;


import com.example.recrutement.entities.TestCase;
import com.example.recrutement.services.TestCaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/testcases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping
    public List<TestCase> getAllTestCases() {
        return testCaseService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCaseById(@PathVariable Integer id) {
        return testCaseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return testCaseService.save(testCase);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCase> updateTestCase(@PathVariable Integer id, @RequestBody TestCase updatedTestCase) {
        return testCaseService.findById(id)
                .map(testCase -> {
                    updatedTestCase.setId(testCase.getId());
                    TestCase saved = testCaseService.save(updatedTestCase);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Integer id) {
        if (testCaseService.findById(id).isPresent()) {
            testCaseService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
