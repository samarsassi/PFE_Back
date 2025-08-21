package com.example.recrutement.flowable;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlowableRunner implements CommandLineRunner {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;

    public FlowableRunner(RuntimeService runtimeService, RepositoryService repositoryService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if there are any deployed process definitions
            var processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .list();

            if (processDefinitions.isEmpty()) {
                System.out.println("No process definitions found. Skipping automatic process start.");
                return;
            }

            // Get the latest deployed process definition
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                    .orderByProcessDefinitionVersion()
                    .desc()
                    .list()
                    .get(0);

            if (processDefinition != null) {
                System.out.println("Process definition found: " + processDefinition.getId() +
                        " (version: " + processDefinition.getVersion() + ")");
                System.out.println(
                        "Automatic process starting is disabled. Use /api/workflows/start to start processes manually.");

                // Commented out automatic process starting to prevent errors
                // runtimeService.startProcessInstanceById(processDefinition.getId());
                // System.out.println("Dynamic Flowable process started from deployment: " +
                // processDefinition.getDeploymentId());
            }
        } catch (Exception e) {
            System.err.println("Error during Flowable startup check: " + e.getMessage());
            // Don't throw the exception to prevent application startup failure
        }
    }
}
