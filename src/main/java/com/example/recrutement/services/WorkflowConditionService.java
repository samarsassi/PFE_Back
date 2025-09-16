package com.example.recrutement.services;

import com.example.recrutement.entities.WorkflowCondition;
import com.example.recrutement.repositories.WorkflowConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WorkflowConditionService {
    
    private final WorkflowConditionRepository conditionRepository;
    
    @Autowired
    public WorkflowConditionService(WorkflowConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }
    
    /**
     * Get all active conditions for a specific gateway
     */
    public List<WorkflowCondition> getConditionsByGateway(String gatewayId) {
        return conditionRepository.findActiveConditionsByGateway(gatewayId);
    }
    
    /**
     * Get a specific condition by flow ID
     */
    public Optional<WorkflowCondition> getConditionByFlowId(String flowId) {
        return conditionRepository.findByFlowIdAndIsActiveTrue(flowId);
    }
    
    /**
     * Create or update a workflow condition
     */
    @Transactional
    public WorkflowCondition saveCondition(WorkflowCondition condition) {
        // If updating existing condition, deactivate the old one first
        if (condition.getId() == null && conditionRepository.existsByFlowIdAndIsActiveTrue(condition.getFlowId())) {
            deactivateConditionByFlowId(condition.getFlowId());
        }
        return conditionRepository.save(condition);
    }
    
    /**
     * Deactivate a condition by flow ID
     */
    @Transactional
    public void deactivateConditionByFlowId(String flowId) {
        Optional<WorkflowCondition> existing = conditionRepository.findByFlowIdAndIsActiveTrue(flowId);
        if (existing.isPresent()) {
            WorkflowCondition condition = existing.get();
            condition.setIsActive(false);
            conditionRepository.save(condition);
        }
    }
    
    /**
     * Deactivate a condition by ID
     */
    @Transactional
    public void deactivateCondition(Long conditionId) {
        Optional<WorkflowCondition> condition = conditionRepository.findById(conditionId);
        if (condition.isPresent()) {
            WorkflowCondition wc = condition.get();
            wc.setIsActive(false);
            conditionRepository.save(wc);
        }
    }
    
    /**
     * Get all distinct gateway IDs that have conditions
     */
    public List<String> getAllGatewayIds() {
        return conditionRepository.findDistinctActiveGatewayIds();
    }
    
    /**
     * Initialize default conditions for the CV Score Decision gateway
     */
    @Transactional
    public void initializeDefaultConditions() {
        // Check if default conditions already exist
        if (conditionRepository.findByGatewayIdAndIsActiveTrue("CV_Score_Decision").isEmpty()) {
            // Create default condition for Flow_3 (Send Challenge)
            WorkflowCondition challengeCondition = new WorkflowCondition(
                "CV_Score_Decision",
                "Flow_3",
                "${cvScore >= 3}",
                "High Score - Send Challenge"
            );
            challengeCondition.setDescription("Candidate CV score is 3 or higher - proceed to challenge");
            conditionRepository.save(challengeCondition);
            
            // Create default condition for Flow_4 (Reject)
            WorkflowCondition rejectCondition = new WorkflowCondition(
                "CV_Score_Decision",
                "Flow_4",
                "${cvScore < 3}",
                "Low Score - Reject"
            );
            rejectCondition.setDescription("Candidate CV score is less than 3 - reject candidature");
            conditionRepository.save(rejectCondition);
        }
    }
    
    /**
     * Get the condition expression for a specific flow
     */
    public String getConditionExpression(String flowId) {
        Optional<WorkflowCondition> condition = conditionRepository.findByFlowIdAndIsActiveTrue(flowId);
        return condition.map(WorkflowCondition::getConditionExpression).orElse("");
    }
}
