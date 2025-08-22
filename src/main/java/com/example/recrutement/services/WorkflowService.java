package com.example.recrutement.services;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkflowService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    public WorkflowService(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    /**
     * Validate BPMN XML content
     */
    private void validateBpmnXml(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new IllegalArgumentException("BPMN XML content cannot be null or empty");
        }

        // Basic XML structure validation
        if (!bpmnXml.contains("<?xml") || !bpmnXml.contains("<bpmn:definitions")) {
            throw new IllegalArgumentException("Invalid BPMN XML format - missing XML declaration or bpmn:definitions");
        }

        // Check for required BPMN elements
        if (!bpmnXml.contains("<bpmn:process") || !bpmnXml.contains("isExecutable=\"true\"")) {
            throw new IllegalArgumentException("Invalid BPMN XML - process must be executable");
        }

        // Check for at least one start event
        if (!bpmnXml.contains("<bpmn:startEvent")) {
            throw new IllegalArgumentException("Invalid BPMN XML - must contain at least one start event");
        }

        // Validate service tasks have delegate expressions
        validateServiceTaskDelegates(bpmnXml);
    }

    /**
     * Remove UTF-8 BOM and any leading whitespace/newlines before the XML declaration.
     * Ensures the content starts exactly with "<?xml" so Flowable's parser accepts it.
     */
    private String sanitizeXml(String xml) {
        if (xml == null) return null;
        // Remove UTF-8 BOM if present
        String cleaned = xml.replaceFirst("^\uFEFF", "");
        // Trim leading whitespace/newlines before XML declaration
        cleaned = cleaned.replaceFirst("^\\s+", "");
        return cleaned;
    }

    /**
     * Validate that all service tasks have proper delegate expressions
     */
    private void validateServiceTaskDelegates(String bpmnXml) {
        // Check if there are service tasks
        if (bpmnXml.contains("<bpmn:serviceTask")) {
            // Check if service tasks have delegate expressions
            if (!bpmnXml.contains("flowable:delegateExpression")) {
                throw new IllegalArgumentException(
                        "Service tasks found but no delegate expressions assigned. Please assign delegates to all service tasks.");
            }

            // Count service tasks and delegate expressions
            long serviceTaskCount = bpmnXml.chars()
                    .mapToObj(i -> (char) i)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString()
                    .split("<bpmn:serviceTask").length - 1;

            long delegateExpressionCount = bpmnXml.chars()
                    .mapToObj(i -> (char) i)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString()
                    .split("flowable:delegateExpression").length - 1;

            if (serviceTaskCount > delegateExpressionCount) {
                throw new IllegalArgumentException("Not all service tasks have delegate expressions assigned. Found " +
                        serviceTaskCount + " service tasks but only " + delegateExpressionCount
                        + " delegate expressions.");
            }
        }
    }

    public void deployAndStartWorkflow(String bpmnXml) {
        // Sanitize and validate the XML first
        String cleanXml = sanitizeXml(bpmnXml);
        validateBpmnXml(cleanXml);

        // Persist BPMN into classpath resources so it's the single source of truth
        try {
            Path resourcesPath = Paths.get("src/main/resources/processes");
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }
            Path bpmnFile = resourcesPath.resolve("Recruitment_Workflow.bpmn20.xml");
            Files.write(bpmnFile, cleanXml.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("BPMN XML saved to: " + bpmnFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save BPMN file: " + e.getMessage());
        }

        try {
            // Delete all previous deployments to ensure only the latest is used
            var existingDeployments = repositoryService.createDeploymentQuery()
                    .deploymentName("Static Recruitment Workflow")
                    .list();

            for (Deployment deployment : existingDeployments) {
                System.out.println("Deleting old deployment: " + deployment.getId());
                repositoryService.deleteDeployment(deployment.getId(), true);
            }

            // Deploy the new workflow
            Deployment newDeployment = repositoryService.createDeployment()
                    .addString("Recruitment_Workflow.bpmn20.xml", cleanXml)
                    .name("Static Recruitment Workflow")
                    .deploy();

            System.out.println("New deployment created: " + newDeployment.getId());

            // Verify the deployment was successful
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(newDeployment.getId())
                    .singleResult();

            if (processDefinition != null) {
                System.out.println("Process definition deployed successfully: " + processDefinition.getId());
                System.out.println("Process key: " + processDefinition.getKey());
                System.out.println("Process version: " + processDefinition.getVersion());

                // Optionally start a process instance (you can make this conditional)
                try {
                    runtimeService.startProcessInstanceById(processDefinition.getId());
                    System.out.println("Dynamic Flowable process started from the latest BPMN!");
                } catch (Exception ex) {
                    System.err.println("Failed to start process instance: " + ex.getMessage());
                    // Don't throw - deployment was successful, just couldn't start instance
                }
            } else {
                throw new RuntimeException("Failed to deploy process: no definition found in deployment");
            }

        } catch (Exception e) {
            System.err.println("Deployment failed: " + e.getMessage());
            throw new RuntimeException("Failed to deploy workflow: " + e.getMessage(), e);
        }
    }

    /**
     * Deploy workflow without starting a process instance
     */
    public void deployWorkflowOnly(String bpmnXml) {
        // Sanitize and validate the XML first
        String cleanXml = sanitizeXml(bpmnXml);
        validateBpmnXml(cleanXml);

        // Persist BPMN into classpath resources so it's the single source of truth
        try {
            Path resourcesPath = Paths.get("src/main/resources/processes");
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }
            Path bpmnFile = resourcesPath.resolve("Recruitment_Workflow.bpmn20.xml");
            Files.write(bpmnFile, cleanXml.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("BPMN XML saved to: " + bpmnFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save BPMN file: " + e.getMessage());
        }

        try {
            // Delete all previous deployments to ensure only the latest is used
            var existingDeployments = repositoryService.createDeploymentQuery()
                    .deploymentName("Static Recruitment Workflow")
                    .list();

            for (Deployment deployment : existingDeployments) {
                System.out.println("Deleting old deployment: " + deployment.getId());
                repositoryService.deleteDeployment(deployment.getId(), true);
            }

            // Deploy the new workflow
            Deployment newDeployment = repositoryService.createDeployment()
                    .addString("Recruitment_Workflow.bpmn20.xml", cleanXml)
                    .name("Static Recruitment Workflow")
                    .deploy();

            System.out.println("New deployment created: " + newDeployment.getId());

            // Verify the deployment was successful
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(newDeployment.getId())
                    .singleResult();

            if (processDefinition != null) {
                System.out.println("Process definition deployed successfully: " + processDefinition.getId());
                System.out.println("Process key: " + processDefinition.getKey());
                System.out.println("Process version: " + processDefinition.getVersion());
            } else {
                throw new RuntimeException("Failed to deploy process: no definition found in deployment");
            }

        } catch (Exception e) {
            System.err.println("Deployment failed: " + e.getMessage());
            throw new RuntimeException("Failed to deploy workflow: " + e.getMessage(), e);
        }
    }

    /**
     * Get the latest deployed process definition
     */
    public String getLatestProcessDefinitionId() {
        var processDefinition = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc()
                .list()
                .stream()
                .findFirst()
                .orElse(null);

        return processDefinition != null ? processDefinition.getId() : null;
    }

    /**
     * Start a process instance with the latest deployed definition
     */
    public void startLatestProcess() {
        String processDefinitionId = getLatestProcessDefinitionId();
        if (processDefinitionId != null) {
            try {
                runtimeService.startProcessInstanceById(processDefinitionId);
                System.out.println("Started process instance with definition: " + processDefinitionId);
            } catch (Exception e) {
                System.err.println("Failed to start process instance: " + e.getMessage());
                throw new RuntimeException("Failed to start process instance", e);
            }
        } else {
            throw new RuntimeException("No process definition found to start");
        }
    }

    /**
     * Start a process instance of the latest deployed definition with given variables
     */
    public void startLatestProcessWithVariables(Map<String, Object> variables) {
        String processDefinitionId = getLatestProcessDefinitionId();
        if (processDefinitionId != null) {
            try {
                runtimeService.startProcessInstanceById(processDefinitionId, variables != null ? variables : new HashMap<>());
                System.out.println("Started process instance with variables for definition: " + processDefinitionId);
            } catch (Exception e) {
                System.err.println("Failed to start process instance with variables: " + e.getMessage());
                throw new RuntimeException("Failed to start process instance with variables", e);
            }
        } else {
            throw new RuntimeException("No process definition found to start");
        }
    }

    /**
     * Start a process instance by process definition key with given variables
     */
    public void startProcessByKeyWithVariables(String processKey, Map<String, Object> variables) {
        if (processKey == null || processKey.isBlank()) {
            throw new IllegalArgumentException("processKey is required");
        }
        try {
            runtimeService.startProcessInstanceByKey(processKey, variables != null ? variables : new HashMap<>());
            System.out.println("Started process instance by key: " + processKey);
        } catch (Exception e) {
            System.err.println("Failed to start process by key '" + processKey + "': " + e.getMessage());
            throw new RuntimeException("Failed to start process by key", e);
        }
    }

    /**
     * Start a process instance with variables for CV analysis
     */
    public void startCvAnalysisProcess(Integer candidatureId, Integer offreEmploiId) {
        String processDefinitionId = getLatestProcessDefinitionId();
        if (processDefinitionId != null) {
            try {
                // Create variables map
                Map<String, Object> variables = new HashMap<>();
                variables.put("candidatureId", candidatureId);
                variables.put("offreEmploiId", offreEmploiId);

                runtimeService.startProcessInstanceById(processDefinitionId, variables);
                System.out.println("Started CV analysis process with candidature ID: " + candidatureId +
                        " and offre emploi ID: " + offreEmploiId);
            } catch (Exception e) {
                System.err.println("Failed to start CV analysis process: " + e.getMessage());
                throw new RuntimeException("Failed to start CV analysis process", e);
            }
        } else {
            throw new RuntimeException("No process definition found to start");
        }
    }

    /**
     * Get process definition by ID
     */
    public org.flowable.engine.repository.ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        try {
            return repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
        } catch (Exception e) {
            System.err.println("Failed to get process definition by ID: " + e.getMessage());
            return null;
        }
    }
}
