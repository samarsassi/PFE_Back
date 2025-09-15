package com.example.recrutement.controllers;

import com.example.recrutement.delegate.DelegateDescription;
import com.example.recrutement.services.WorkflowService;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.core.io.ClassPathResource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final ApplicationContext applicationContext;
    private final RepositoryService repositoryService;

    @Autowired
    public WorkflowController(WorkflowService workflowService,
                              ApplicationContext applicationContext,
                              RepositoryService repositoryService) {
        this.workflowService = workflowService;
        this.applicationContext = applicationContext;
        this.repositoryService = repositoryService;
    }

    @GetMapping("/delegates")
    public ResponseEntity<List<DelegateInfo>> getAvailableDelegates() {
        // 1. Find all beans that implement JavaDelegate
        Map<String, JavaDelegate> delegateBeans = applicationContext.getBeansOfType(JavaDelegate.class);

        // 2. Convert them into DTOs
        List<DelegateInfo> delegates = delegateBeans.entrySet().stream()
                .map(entry -> {
                    String beanName = entry.getKey();
                    Class<?> clazz = entry.getValue().getClass();

                    // Optionally check for @Component("name") or Javadoc for description
                    String description = clazz.getSimpleName();

                    if (clazz.isAnnotationPresent(DelegateDescription.class)) {
                        description = clazz.getAnnotation(DelegateDescription.class).value();
                    }
                    return new DelegateInfo(beanName, clazz.getName(), description);
                })
                .toList();

        return ResponseEntity.ok(delegates);
    }

    @GetMapping("/latest")
    public ResponseEntity<String> getLatestProcessDefinitionXml() {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .singleResult();

            if (processDefinition == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No process definitions found");
            }

            // Prefer reading by processDefinitionId (more robust across deployment types)
            try (InputStream modelStream = repositoryService.getProcessModel(processDefinition.getId())) {
                if (modelStream != null) {
                    String xml = new String(modelStream.readAllBytes(), StandardCharsets.UTF_8);
                    return ResponseEntity.ok(xml);
                }
            }

            // Fallback to resource by deployment/resource name
            try (InputStream resStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(), processDefinition.getResourceName())) {
                if (resStream == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Resource not found for deployment: " + processDefinition.getDeploymentId());
                }
                String xml = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);
                return ResponseEntity.ok(xml);
            }
        } catch (Exception e) {
            try {
                // Fallback: read static classpath resource if repository retrieval failed
                ClassPathResource cpr = new ClassPathResource("processes/Recruitment_Workflow_LATEST.bpmn20.xml");
                if (cpr.exists()) {
                    try (InputStream is = cpr.getInputStream()) {
                        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                                .replaceFirst("^\\uFEFF", "")
                                .replaceFirst("^\\s+", "");
                        return ResponseEntity.ok(xml);
                    }
                }
            } catch (Exception ignore) {
                // fall through to 500
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve latest process definition: " + e.getMessage());
        }
    }


    @PostMapping("/deploy")
    public ResponseEntity<String> deployWorkflow(@RequestBody Map<String, String> payload) {
        try {
            String bpmnXml = payload.get("xml");
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("XML is missing or empty");
            }
            // Apply conditions from DB
            String xmlWithConditions = workflowService.applyWorkflowConditions(bpmnXml);
            // Deploy without starting
            workflowService.deployWorkflowOnly(xmlWithConditions);
            return ResponseEntity.ok("Workflow deployed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deploy workflow: " + e.getMessage());
        }
    }

    @PostMapping("/deploy-only")
    public ResponseEntity<String> deployWorkflowOnly(@RequestBody String bpmnXml) {
        try {
            workflowService.deployWorkflowOnly(bpmnXml);
            return ResponseEntity.ok("Workflow deployed successfully (process not started)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deploy workflow: " + e.getMessage());
        }
    }

    @PostMapping("/start")
    public ResponseEntity<String> startLatestProcess() {
        try {
            workflowService.startLatestProcess();
            return ResponseEntity.ok("Process started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start process: " + e.getMessage());
        }
    }



    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            String latestProcessId = workflowService.getLatestProcessDefinitionId();
            status.put("latestProcessDefinitionId", latestProcessId);
            status.put("hasDeployedProcess", latestProcessId != null);
            status.put("status", "OK");

            if (latestProcessId != null) {
                var processDefinition = workflowService.getProcessDefinitionById(latestProcessId);
                if (processDefinition != null) {
                    status.put("processKey", processDefinition.getKey());
                    status.put("processVersion", processDefinition.getVersion());
                    status.put("deploymentId", processDefinition.getDeploymentId());
                }
            }
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/start-with-vars")
    public ResponseEntity<String> startWithVariables(@RequestBody Map<String, Object> variables) {
        try {
            // Validate minimal variables based on current BPMN delegates
            // emailDelegate needs: candidateEmail, emailSubject, emailBody
            // sendChallengeDelegate needs: candidatureId, challengeId, candidateEmail
            if (variables == null) variables = new HashMap<>();

            String[] required = new String[] {"candidateEmail", "emailSubject", "emailBody", "candidatureId", "challengeId"};
            for (String key : required) {
                if (!variables.containsKey(key)) {
                    return ResponseEntity.badRequest().body("Missing required variable: " + key);
                }
            }

            workflowService.startLatestProcessWithVariables(variables);
            return ResponseEntity.ok("Process started successfully with variables");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start process with variables: " + e.getMessage());
        }
    }



    @JsonPropertyOrder({ "className", "beanName", "description" })
    public static class DelegateInfo {
        private String beanName;
        private String description;
        private String className;

        public DelegateInfo(String beanName, String className, String description) {
            this.beanName = beanName;
            this.className = className;
            this.description = description;
        }

        public String getDescription() { return description; }
        public String getBeanName() { return beanName; }
        public String getClassName() { return className; }
    }


    @PostMapping("/start-by-key/{processKey}")
    public ResponseEntity<String> startByKey(@PathVariable String processKey, @RequestBody Map<String, Object> variables) {
        try {
            if (variables == null) variables = new HashMap<>();
            // recommend core vars for recruitment_process
            if ("recruitment_process".equals(processKey)) {
                String[] required = new String[] {"candidatureId", "offreEmploiId", "candidateEmail", "challengeId", "hrEmail", "interviewerEmail"};
                for (String key : required) {
                    if (!variables.containsKey(key)) {
                        return ResponseEntity.badRequest().body("Missing required variable: " + key);
                    }
                }
            }
            // start using key
            workflowService.startProcessByKeyWithVariables(processKey, variables);
            return ResponseEntity.ok("Process started: " + processKey);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start process by key: " + e.getMessage());
        }
    }

    // ------------------ HISTORY API ------------------

    @GetMapping("/history")
    public ResponseEntity<List<String>> getHistoryVersions() {
        try {
            List<String> historyFiles = workflowService.listHistoryVersions();
            return ResponseEntity.ok(historyFiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Failed to list BPMN history: " + e.getMessage()));
        }
    }

    @GetMapping("/history/{filename}")
    public ResponseEntity<String> getHistoryVersion(@PathVariable String filename) {
        try {
            String xml = workflowService.getHistoryVersionXml(filename);
            return ResponseEntity.ok(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load BPMN history version: " + e.getMessage());
        }
    }


    @PostMapping("/history/deploy")
    public ResponseEntity<String> deployHistoryVersion(@RequestBody Map<String, String> payload) {
        String fileName = payload.get("fileName");
        if (fileName == null || fileName.isBlank()) {
            return ResponseEntity.badRequest().body("fileName is required");
        }
        try {
            workflowService.deployHistoryVersion(fileName);
            return ResponseEntity.ok("Deployed historical BPMN version: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deploy historical BPMN version: " + e.getMessage());
        }
    }


    /**
     * Apply workflow conditions to existing BPMN XML
     * This allows admins to update conditions without redeploying the entire workflow
     */
    @PostMapping("/apply-conditions")
    public ResponseEntity<String> applyConditionsToBpmn(@RequestBody String bpmnXml) {
        try {
            String xmlWithConditions = workflowService.applyWorkflowConditions(bpmnXml);
            return ResponseEntity.ok(xmlWithConditions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to apply workflow conditions: " + e.getMessage());
        }
    }
}