package com.example.recrutement.controllers;

import com.example.recrutement.services.WorkflowService;
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
        List<DelegateInfo> delegates = new ArrayList<>();
        delegates.add(new DelegateInfo(
                "cvAnalysisDelegate",
                "com.example.recrutement.delegate.CvAnalysisDelegate",
                "Analyzes the candidate's CV using ScoringService and sets the cvScore process variable."
        ));
        delegates.add(new DelegateInfo(
                "emailDelegate",
                "com.example.recrutement.delegate.EmailDelegate",
                "Sends HTML emails (confirmation, rejection, acceptance, interview invites) using EmailService."
        ));
        delegates.add(new DelegateInfo(
                "sendChallengeDelegate",
                "com.example.recrutement.delegate.SendChallengeDelegate",
                "Assigns a coding challenge, updates Candidature and SoumissionDefi, and sends a notification email."
        ));
        delegates.add(new DelegateInfo(
                "rejectCandidatureDelegate",
                "com.example.recrutement.delegate.RejectCandidatureDelegate",
                "Sets the Candidature status to REFUSEE for rejections (CV score, HR decision, or timeout)."
        ));
        delegates.add(new DelegateInfo(
                "evaluateChallengeDelegate",
                "com.example.recrutement.delegate.EvaluateChallengeDelegate",
                "Evaluates the challenge submission and sets the challengePassed process variable."
        ));
        delegates.add(new DelegateInfo(
                "sendInterviewInviteDelegate",
                "com.example.recrutement.delegate.SendInterviewInviteDelegate",
                "Sends an interview invitation email with the scheduled date."
        ));

        Map<String, JavaDelegate> delegateBeans = applicationContext.getBeansOfType(JavaDelegate.class);
        delegateBeans.forEach((beanName, bean) -> {
            if (!delegates.stream().anyMatch(d -> d.getBeanName().equals(beanName))) {
                delegates.add(new DelegateInfo(
                        beanName,
                        bean.getClass().getName(),
                        "Auto-detected JavaDelegate: " + bean.getClass().getSimpleName()
                ));
            }
        });

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
                ClassPathResource cpr = new ClassPathResource("processes/Recruitment_Workflow.bpmn20.xml");
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
    public ResponseEntity<String> deployWorkflow(@RequestBody String bpmnXml) {
        try {
            workflowService.deployAndStartWorkflow(bpmnXml);
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

    @PostMapping("/start-cv-analysis")
    public ResponseEntity<String> startCvAnalysisProcess(@RequestBody Map<String, Integer> request) {
        try {
            Integer candidatureId = request.get("candidatureId");
            Integer offreEmploiId = request.get("offreEmploiId");

            if (candidatureId == null || offreEmploiId == null) {
                return ResponseEntity.badRequest()
                        .body("Both candidatureId and offreEmploiId are required");
            }

            workflowService.startCvAnalysisProcess(candidatureId, offreEmploiId);
            return ResponseEntity.ok("CV analysis process started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start CV analysis process: " + e.getMessage());
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

    public static class DelegateInfo {
        private String beanName;
        private String className;
        private String description;

        public DelegateInfo(String beanName, String className, String description) {
            this.beanName = beanName;
            this.className = className;
            this.description = description;
        }

        public String getBeanName() { return beanName; }
        public String getClassName() { return className; }
        public String getDescription() { return description; }
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
}