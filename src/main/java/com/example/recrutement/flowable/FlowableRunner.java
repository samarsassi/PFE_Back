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
        var deployments = repositoryService.createDeploymentQuery()
                .deploymentName("Dynamic Workflow")
                .orderByDeploymentTime()
                .desc()
                .list();

        if (!deployments.isEmpty()) {
            var latestDeployment = deployments.get(0);
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(latestDeployment.getId())
                    .singleResult();

            if (processDefinition != null) {
                runtimeService.startProcessInstanceById(processDefinition.getId());
                System.out.println("Dynamic Flowable process started!");
            }
        } else {
            System.out.println("No workflow deployed yet.");
        }
    }
}
