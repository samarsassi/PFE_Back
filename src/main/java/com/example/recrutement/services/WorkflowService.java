package com.example.recrutement.services;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class WorkflowService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    public WorkflowService(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    public void deployAndStartWorkflow(String bpmnXml) {
        try {
            // Save BPMN to resources folder
            Path resourcesPath = Paths.get("src/main/resources/processes");
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }

            Path bpmnFile = resourcesPath.resolve("Dynamic_Workflow.bpmn20.xml");
            Files.write(bpmnFile, bpmnXml.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("BPMN XML saved to: " + bpmnFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save BPMN file: " + e.getMessage());
            return;
        }

        // Delete all previous deployments to ensure only the latest is used
        repositoryService.createDeploymentQuery()
                .list()
                .forEach(deployment -> {
                    System.out.println("Deleting old deployment: " + deployment.getId());
                    repositoryService.deleteDeployment(deployment.getId(), true);
                });

        // Deploy the new workflow
        Deployment newDeployment = repositoryService.createDeployment()
                .addString("Dynamic_Workflow.bpmn20.xml", bpmnXml)
                .name("Dynamic Workflow")
                .deploy();

        System.out.println("New deployment created: " + newDeployment.getId());

        // Start the process from the new deployment
        var processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(newDeployment.getId())
                .singleResult();

        if (processDefinition != null) {
            try {
                runtimeService.startProcessInstanceById(processDefinition.getId());
                System.out.println("Dynamic Flowable process started from the latest BPMN!");
            } catch (Exception ex) {
                System.err.println("Failed to start process: " + ex.getMessage());
            }
        } else {
            System.err.println("Failed to start process: no definition found in deployment");
        }
    }
}
