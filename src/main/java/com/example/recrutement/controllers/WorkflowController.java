package com.example.recrutement.controllers;

import com.example.recrutement.services.WorkflowService;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final ApplicationContext applicationContext;
    @Autowired
    public WorkflowController(WorkflowService workflowService,
                              ApplicationContext applicationContext) {
        this.workflowService = workflowService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/delegates")
    public ResponseEntity<Map<String, String>> getAvailableDelegates() {
        Map<String, String> delegates = new HashMap<>();

        // Manual example, if you still want hardcoded
        delegates.put("com.example.recrutement.delegate.CvAnalysisDelegate", "CV Analysis Delegate");

        // Automatically discover Spring beans that implement JavaDelegate
        Map<String, JavaDelegate> delegateBeans = applicationContext.getBeansOfType(JavaDelegate.class);
        delegateBeans.values().forEach(bean -> {
            String className = bean.getClass().getName();
            String simpleName = bean.getClass().getSimpleName();
            delegates.put(className, simpleName);
        });

        return ResponseEntity.ok(delegates);
    }

    @PostMapping("/deploy")
    public ResponseEntity<String> deployWorkflow(@RequestBody String bpmnXml) {
        try {
            workflowService.deployAndStartWorkflow(bpmnXml);
            return ResponseEntity.ok("Workflow deployed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
