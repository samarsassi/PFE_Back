package com.example.recrutement.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("MyTaskDelegate")
public class MyTaskDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("MyTaskDelegate is running!");
        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        System.out.println("Received candidatureId: " + candidatureId);

        // Just set a dummy process variable
        execution.setVariable("cvScore", 100);
    }
}
