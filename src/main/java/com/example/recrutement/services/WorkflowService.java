package com.example.recrutement.services;

import com.example.recrutement.entities.WorkflowCondition;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorkflowService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final WorkflowConditionService conditionService;

    private final Path processesDir = Paths.get("src/main/resources/processes");
    private final Path historyDir = processesDir.resolve("history");
    private final Path latestFile = processesDir.resolve("Recruitment_Workflow_LATEST.bpmn20.xml");

    private final Map<String, String> latestDeployedIds = new HashMap<>();
    private static final String STABLE_KEY = "Process_1";

    public WorkflowService(RepositoryService repositoryService, RuntimeService runtimeService, WorkflowConditionService conditionService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.conditionService = conditionService;
        ensureDirs();
    }

    // ------------------ DEPLOYMENT ------------------

    public void deployAndStartWorkflow(String bpmnXml) {
        String xmlWithConditions = applyWorkflowConditions(bpmnXml);
        String cleanXml = sanitizeXml(xmlWithConditions);
        validateBpmnXml(cleanXml);

        String uniqueId = generateUniqueProcessId();
        cleanXml = replaceProcessId(cleanXml, uniqueId);
        saveBpmnXmlVersion(cleanXml);

        deleteOldDeployments();

        Deployment newDeployment = repositoryService.createDeployment()
                .addString("Recruitment_Workflow.bpmn20.xml", cleanXml)
                .name("Static Recruitment Workflow")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(newDeployment.getId())
                .singleResult();

        if (processDefinition != null) {
            latestDeployedIds.put(STABLE_KEY, uniqueId);
            runtimeService.startProcessInstanceById(processDefinition.getId());
            System.out.println("Process started with ID: " + uniqueId);
        } else {
            throw new RuntimeException("Failed to deploy process: no definition found");
        }
    }

    public void deployWorkflowOnly(String bpmnXml) {
        String xmlWithConditions = applyWorkflowConditions(bpmnXml);
        String cleanXml = sanitizeXml(xmlWithConditions);
        validateBpmnXml(cleanXml);

        saveBpmnXmlVersion(cleanXml);
        deleteOldDeployments();

        Deployment newDeployment = repositoryService.createDeployment()
                .addString("Recruitment_Workflow_LATEST.bpmn20.xml", cleanXml)
                .name("Static Recruitment Workflow")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(newDeployment.getId())
                .singleResult();

        if (processDefinition != null) {
            latestDeployedIds.put(STABLE_KEY, processDefinition.getKey());
            System.out.println("Process deployed successfully: " + processDefinition.getKey());
        } else {
            throw new RuntimeException("Failed to deploy process: no definition found");
        }
    }


    // ------------------ PROCESS START ------------------

    public void startLatestProcess() {
        String processDefinitionId = getLatestProcessDefinitionId();
        if (processDefinitionId != null) {
            runtimeService.startProcessInstanceById(processDefinitionId);
        } else {
            throw new RuntimeException("No process definition found to start");
        }
    }

    public void startProcessByKeyWithVariables(String processKey, Map<String, Object> variables) {
        String latestUniqueId = latestDeployedIds.getOrDefault(processKey, processKey);
        runtimeService.startProcessInstanceByKey(latestUniqueId, variables != null ? variables : new HashMap<>());
    }

    public void startLatestProcessWithVariables(Map<String, Object> variables) {
        String processDefinitionId = getLatestProcessDefinitionId();
        if (processDefinitionId != null) {
            try {
                runtimeService.startProcessInstanceById(processDefinitionId, variables != null ? variables : new HashMap<>());
                System.out.println("Started process with variables: " + processDefinitionId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to start process instance with variables", e);
            }
        } else {
            throw new RuntimeException("No process definition found to start");
        }
    }

    // ------------------ HELPERS ------------------

    private void ensureDirs() {
        try {
            if (!Files.exists(historyDir)) Files.createDirectories(historyDir);
            if (!Files.exists(processesDir)) Files.createDirectories(processesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories for BPMN storage", e);
        }
    }

    private String generateUniqueProcessId() {
        return STABLE_KEY + "_" + System.currentTimeMillis();
    }

    private String replaceProcessId(String xml, String newId) {
        return xml.replaceAll("id\\s*=\\s*\"Process_1\"", "id=\"" + newId + "\"");
    }

    private void deleteOldDeployments() {
        var deployments = repositoryService.createDeploymentQuery()
                .deploymentName("Static Recruitment Workflow")
                .list();
        for (Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    private String sanitizeXml(String xml) {
        if (xml == null) return null;
        return xml.replaceFirst("^\uFEFF", "").replaceFirst("^\\s+", "");
    }

    private void validateBpmnXml(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.trim().isEmpty())
            throw new IllegalArgumentException("BPMN XML content cannot be null or empty");
        if (!bpmnXml.contains("<?xml") || !bpmnXml.contains("<bpmn:definitions"))
            throw new IllegalArgumentException("Invalid BPMN XML format");
        if (!bpmnXml.contains("<bpmn:process") || !bpmnXml.contains("isExecutable=\"true\""))
            throw new IllegalArgumentException("Invalid BPMN XML - process must be executable");
        if (!bpmnXml.contains("<bpmn:startEvent"))
            throw new IllegalArgumentException("Invalid BPMN XML - must contain at least one start event");
    }

    // ------------------ VERSIONING ------------------

    private void saveBpmnXmlVersion(String xml) {
        try {
            String fileName = "Recruitment_Workflow_" + System.currentTimeMillis() + ".bpmn20.xml";
            Path versionedFile = historyDir.resolve(fileName);
            Files.write(versionedFile, xml.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

            Files.write(latestFile, xml.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            cleanupOldHistory();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save BPMN snapshot", e);
        }
    }

    private void cleanupOldHistory() throws IOException {
        try (Stream<Path> files = Files.list(historyDir)) {
            List<Path> sortedFiles = files
                    .filter(f -> f.getFileName().toString().startsWith("Recruitment_Workflow_"))
                    .sorted(Comparator.comparingLong((Path f) -> f.toFile().lastModified()).reversed())

                    .collect(Collectors.toList());

            for (int i = 4; i < sortedFiles.size(); i++) {
                Files.deleteIfExists(sortedFiles.get(i));
            }
        }
    }

    // ------------------ CONDITIONS ------------------

    public String applyWorkflowConditions(String bpmnXml) {
        // TODO: apply DB-driven conditions dynamically
        return bpmnXml;
    }

    // ------------------ GETTERS ------------------

    public String getLatestProcessDefinitionId() {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc()
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        return processDefinition != null ? processDefinition.getId() : null;
    }

    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        try {
            return repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
        } catch (Exception e) {
            System.err.println("Failed to get process definition by ID: " + e.getMessage());
            return null;
        }
    }

    // ------------------ HISTORY SELECTION ------------------

    public List<String> listHistoryVersions() {
        try (Stream<Path> files = Files.list(historyDir)) {
            return files
                    .filter(f -> f.getFileName().toString().startsWith("Recruitment_Workflow_"))
                    .sorted(Comparator.comparingLong((Path f) -> f.toFile().lastModified()).reversed())
                    .map(f -> f.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list BPMN history files", e);
        }
    }

    public String getHistoryVersionXml(String filename) {
        Path filePath = historyDir.resolve(filename);
        if (!Files.exists(filePath)) throw new RuntimeException("History file not found: " + filename);
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read BPMN history file: " + filename, e);
        }
    }

    public void deployHistoryVersion(String fileName) {
        Path filePath = historyDir.resolve(fileName);
        if (!Files.exists(filePath)) throw new RuntimeException("BPMN history file not found: " + fileName);
        try {
            String xml = Files.readString(filePath, StandardCharsets.UTF_8);
            String uniqueId = generateUniqueProcessId();
            xml = replaceProcessId(xml, uniqueId);

            Deployment newDeployment = repositoryService.createDeployment()
                    .addString("Recruitment_Workflow_LATEST.bpmn20.xml", xml)
                    .name("Static Recruitment Workflow")
                    .deploy();

            latestDeployedIds.put(STABLE_KEY, uniqueId);
            System.out.println("Deployed historical BPMN: " + fileName + " as " + uniqueId);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read BPMN history file: " + fileName, e);
        }
    }
}
