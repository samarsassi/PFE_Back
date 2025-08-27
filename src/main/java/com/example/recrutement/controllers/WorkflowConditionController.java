package com.example.recrutement.controllers;

import com.example.recrutement.entities.WorkflowCondition;
import com.example.recrutement.services.WorkflowConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow-conditions")
public class WorkflowConditionController {
    
    private final WorkflowConditionService conditionService;
    
    @Autowired
    public WorkflowConditionController(WorkflowConditionService conditionService) {
        this.conditionService = conditionService;
    }
    
    /**
     * Get all conditions for a specific gateway
     */
    @GetMapping("/gateway/{gatewayId}")
    public ResponseEntity<List<WorkflowCondition>> getConditionsByGateway(@PathVariable String gatewayId) {
        try {
            List<WorkflowCondition> conditions = conditionService.getConditionsByGateway(gatewayId);
            return ResponseEntity.ok(conditions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all gateway IDs that have conditions
     */
    @GetMapping("/gateways")
    public ResponseEntity<List<String>> getAllGatewayIds() {
        try {
            List<String> gatewayIds = conditionService.getAllGatewayIds();
            return ResponseEntity.ok(gatewayIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create or update a condition
     */
    @PostMapping
    public ResponseEntity<WorkflowCondition> saveCondition(@RequestBody WorkflowCondition condition) {
        try {
            WorkflowCondition savedCondition = conditionService.saveCondition(condition);
            return ResponseEntity.ok(savedCondition);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update a specific condition
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowCondition> updateCondition(@PathVariable Long id, @RequestBody WorkflowCondition condition) {
        try {
            condition.setId(id);
            WorkflowCondition updatedCondition = conditionService.saveCondition(condition);
            return ResponseEntity.ok(updatedCondition);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deactivate a condition
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateCondition(@PathVariable Long id) {
        try {
            conditionService.deactivateCondition(id);
            return ResponseEntity.ok("Condition deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Initialize default conditions
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaultConditions() {
        try {
            conditionService.initializeDefaultConditions();
            return ResponseEntity.ok("Default conditions initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to initialize default conditions: " + e.getMessage());
        }
    }
    
    /**
     * Get condition expression for a specific flow
     */
    @GetMapping("/flow/{flowId}/expression")
    public ResponseEntity<Map<String, String>> getConditionExpression(@PathVariable String flowId) {
        try {
            String expression = conditionService.getConditionExpression(flowId);
            return ResponseEntity.ok(Map.of("expression", expression));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
