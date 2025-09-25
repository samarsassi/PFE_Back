# Workflow Deployment Guide

## Problem Solved

The original issue was that the backend application was failing to start because:
1. **FlowableRunner** was trying to start a process on application startup
2. **WorkflowService** was deploying new XML but not handling errors properly
3. **No validation** of BPMN XML before deployment
4. **No separation** between deployment and process starting

## Solution Implemented

### 1. Improved FlowableRunner
- **Conditional process starting**: Only starts a process if one exists
- **Better error handling**: Won't crash the application if process starting fails
- **Graceful degradation**: Application starts even if no workflows are deployed

### 2. Enhanced WorkflowService
- **XML validation**: Validates BPMN XML structure before deployment
- **Separate deployment methods**: 
  - `deployAndStartWorkflow()` - Deploys and starts a process
  - `deployWorkflowOnly()` - Deploys without starting
- **Better error handling**: More descriptive error messages
- **Process management**: Methods to get latest process and start it manually

### 3. Extended WorkflowController
- **Multiple endpoints** for different use cases:
  - `/api/workflows/deploy` - Deploy and start process
  - `/api/workflows/deploy-only` - Deploy without starting
  - `/api/workflows/start` - Start latest deployed process
  - `/api/workflows/status` - Get deployment status
  - `/api/workflows/delegates` - Get available delegates

## API Endpoints

### 1. Deploy and Start Workflow
```http
POST /api/workflows/deploy
Content-Type: text/plain

<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                  xmlns:flowable="http://flowable.org/bpmn" 
                  targetNamespace="http://www.flowable.org/processdef">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:serviceTask id="Task_1" flowable:delegateExpression="${cvAnalysisDelegate}">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:endEvent id="EndEvent_1">
      <bpmn:incoming>Flow_2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_1" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="EndEvent_1" />
  </bpmn:process>
</bpmn:definitions>
```

### 2. Deploy Only (Without Starting)
```http
POST /api/workflows/deploy-only
Content-Type: text/plain

[Same BPMN XML as above]
```

### 3. Start Latest Process
```http
POST /api/workflows/start
```

### 4. Get Workflow Status
```http
GET /api/workflows/status
```

Response:
```json
{
  "latestProcessDefinitionId": "Process_1:1:12345",
  "hasDeployedProcess": true,
  "status": "OK"
}
```

### 5. Get Available Delegates
```http
GET /api/workflows/delegates
```

Response:
```json
{
  "com.example.recrutement.delegate.CvAnalysisDelegate": "CV Analysis Delegate",
  "com.example.recrutement.delegate.MyTaskDelegate": "MyTaskDelegate"
}
```

## BPMN XML Requirements

Your BPMN XML must contain:
1. **XML declaration**: `<?xml version="1.0" encoding="UTF-8"?>`
2. **BPMN definitions**: `<bpmn:definitions>`
3. **Executable process**: `<bpmn:process isExecutable="true">`
4. **Start event**: `<bpmn:startEvent>`
5. **Proper namespaces**: Include Flowable namespace

## Recommended Workflow

### For Development/Testing:
1. Use `/api/workflows/deploy-only` to deploy without starting
2. Check status with `/api/workflows/status`
3. Start manually with `/api/workflows/start` when ready

### For Production:
1. Use `/api/workflows/deploy` to deploy and start immediately
2. Monitor logs for deployment success

## Error Handling

The system now provides detailed error messages:
- **Invalid XML**: "Invalid BPMN XML format - missing XML declaration or bpmn:definitions"
- **Missing process**: "Invalid BPMN XML - process must be executable"
- **No start event**: "Invalid BPMN XML - must contain at least one start event"
- **Deployment failure**: "Failed to deploy workflow: [specific error]"

## Logging

The system logs important events:
- Deployment creation and deletion
- Process definition details (ID, key, version)
- Process instance starting
- Error details for debugging

## Frontend Integration

When sending XML from your Camunda Modeler frontend:

1. **Validate XML** before sending
2. **Use deploy-only** for testing
3. **Check status** after deployment
4. **Start process** when ready
5. **Handle errors** gracefully

Example frontend code:
```javascript
async function deployWorkflow(xmlContent) {
  try {
    const response = await fetch('/api/workflows/deploy-only', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: xmlContent
    });
    
    if (response.ok) {
      console.log('Workflow deployed successfully');
      // Check status
      const statusResponse = await fetch('/api/workflows/status');
      const status = await statusResponse.json();
      console.log('Deployment status:', status);
    } else {
      const error = await response.text();
      console.error('Deployment failed:', error);
    }
  } catch (error) {
    console.error('Network error:', error);
  }
}
```

## Troubleshooting

### Application Won't Start
1. Check if there are corrupted deployments in the database
2. Use `/api/workflows/status` to verify deployment state
3. Check application logs for specific error messages

### Process Won't Start
1. Verify XML is valid using the validation
2. Check that all required delegates are available
3. Ensure the process has a valid start event

### Deployment Fails
1. Validate BPMN XML structure
2. Check that all referenced delegates exist
3. Verify XML namespaces are correct 