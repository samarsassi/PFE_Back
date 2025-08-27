package com.example.recrutement.repositories;

import com.example.recrutement.entities.WorkflowCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, Long> {
    
    List<WorkflowCondition> findByGatewayIdAndIsActiveTrue(String gatewayId);
    
    Optional<WorkflowCondition> findByFlowIdAndIsActiveTrue(String flowId);
    
    @Query("SELECT wc FROM WorkflowCondition wc WHERE wc.gatewayId = :gatewayId AND wc.isActive = true ORDER BY wc.conditionName")
    List<WorkflowCondition> findActiveConditionsByGateway(@Param("gatewayId") String gatewayId);
    
    @Query("SELECT DISTINCT wc.gatewayId FROM WorkflowCondition wc WHERE wc.isActive = true")
    List<String> findDistinctActiveGatewayIds();
    
    boolean existsByFlowIdAndIsActiveTrue(String flowId);
}
