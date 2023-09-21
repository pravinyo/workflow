# Using iWF DSL framework to write workflow definition and task on top of Candence/Temporal

## Part 1: Cadencen/Temporal Design
![Cadence System: Deployment Topology](doc/cadence-service.png)
```
Frontend gateway: for rate limiting, routing, authorizing.
History subsystem: maintains data (mutable state, queues, and timers).
Matching subsystem: hosts Task Queues for dispatching.
Worker Service: for internal background Workflows.
```
[To learn more...](https://docs.temporal.io/clusters)

## [Basic Concepts](https://github.com/indeedeng/iwf/wiki/Basic-concepts-overview)

### Runtime platform
Provide the ecosystem to run your applications and takes care of durability, availability, and scalability of the application.
Both Cadence and Temporal share same behaviour as Temporal is forked from Cadence. Worker Processes are hosted by you and execute your code. The communication within Cluster uses gRPC.
Cadence/Temporal service is responsible for keeping workflow state and associated durable timers. It maintains internal queues (called task lists) which are used to dispatch tasks to external workers. Workflow execution is resumable, recoverable, and reactive.
- [Cadence](https://cadenceworkflow.io/docs/get-started/)
- [Temporal](https://docs.temporal.io/temporal)
- [iWF](https://github.com/indeedeng/iwf)


Temporal System Overview for workflow execution

![Temporal service](doc/temporal-service.png)

### Workflows
The term Workflow frequently denotes either a Workflow Type, a Workflow Definition, or a Workflow Execution.
- Workflow Definition: A Workflow Definition is the code that defines the constraints of a Workflow Execution. A Workflow Definition is often also referred to as a Workflow Function.
- Deterministic constraints: A critical aspect of developing Workflow Definitions is ensuring they exhibit certain deterministic traits – that is, making sure that the same Commands are emitted in the same sequence, whenever a corresponding Workflow Function Execution (instance of the Function Definition) is re-executed.
- Handling unreliable Worker Processes: Workflow Function Executions are completely oblivious to the Worker Process in terms of failures or downtime.
- Event Loop:  
  ![img_1.png](doc/workflow-execution.png)
- Workflow execution states:
  ![img.png](doc/workflow-execution-state.png)


### Activities
- An Activity is a normal function or method that executes a single, well-defined action (either short or long running), such as calling another service, transcoding a media file, or sending an email message.
- Workflow code orchestrates the execution of Activities, persisting the results. If an Activity Function Execution fails, any future execution starts from initial state
- Activity Functions are executed by Worker Processes


### Event handling
workflows can be signalled about an external event. A signal is always point to point destined to a specific workflow instance. Signals are always processed in the order in which they are received.
- Human Tasks
- Process Execution Alteration
- Synchronization  
  Example: there is a requirement that all messages for a single user are processed sequentially but the underlying messaging infrastructure can deliver them in parallel. The Cadence solution would be to have a workflow per user and signal it when an event is received. Then the workflow would buffer all signals in an internal data structure and then call an activity for every signal received.

### Visibility
- View,  Filter and Search for Workflow Executions
  - https://docs.temporal.io/visibility#list-filter-examples
  - https://docs.temporal.io/visibility#search-attribute
- Query Workflow state

## Part 2: iWF Design
### High Level Design
An iWF application is composed of several iWF workflow workers. These workers host REST APIs as "worker APIs" for server to call. This callback pattern similar to AWS Step Functions invoking Lambdas, if you are familiar with.

An application also perform actions on workflow executions, such as starting, stopping, signaling, and retrieving results by calling iWF service APIs as "service APIs".

The service APIs are provided by the "API service" in iWF server. Internally, this API service communicates with the Cadence/Temporal service as its backend.
![HLD for iWF](doc/iwf-architecture.png)

### Low Level Design
Users define their workflow code with a new SDK “iWF SDK” and the code is running in workers that talk to the iWF interpreter engine.

The user workflow code defines a list of WorkflowState and kicks off a workflow execution.

At any workflow state, the interpreter will call back the user workflow code to invoke some APIs (waitUntil or execute). Calling the waitUntil API will return some command requests. When the command requests are finished, the interpreter will then call user workflow code to invoke the “execute” API to return a decision. The decision will decide how to complete or transitioning to other workflow states.

At any API, workflow code can mutate the data/search attributes or publish to internal channels.
![LLD for iWF](doc/iwf-workflow-execution.png)

### RPC: Interact with workflow via API 
API for application to interact with the workflow. It can access to persistence, internal channel, and state execution
- [RPC](https://github.com/indeedeng/iwf/wiki/RPC#signal-channel-vs-rpc)
- [RPC vs Signal](https://github.com/indeedeng/iwf/wiki/RPC#signal-channel-vs-rpc)
  - RPC + Internal Channel = Signal Channel
  - Inter Channel and Signal Channel are both message queues
  - RPC is synchronous API call [Definition](https://github.com/indeedeng/iwf-java-sdk/blob/main/src/main/java/io/iworkflow/core/RpcDefinitions.java)
  - Signal channel is Asynchronous API call

### Determinism and Versioning: iWF Approach
- [IWF doc](https://github.com/indeedeng/iwf/wiki/Compare-with-Cadence-Temporal#determinism-and-versioning)
- Use flag to control the code execution as versioning in removed
- Since there is no versioning non-determinism issue will not happen 

### Example: Java workflow definition
```java
public class UserSignupWorkflow implements ObjectWorkflow {

    public static final String DA_FORM = "Form";

    public static final String DA_Status = "Status";
    public static final String VERIFY_CHANNEL = "Verify";

    private MyDependencyService myService;

    public UserSignupWorkflow(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.startingState(new SubmitState(myService)),
                StateDef.nonStartingState(new VerifyState(myService))
        );
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(SignupForm.class, DA_FORM),
                DataAttributeDef.create(String.class, DA_Status)
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return Arrays.asList(
                InternalChannelDef.create(Void.class, VERIFY_CHANNEL)
        );
    }

    // Atomically read/write/send message in RPC
    @RPC
    public String verify(Context context, Persistence persistence, Communication communication) {
        String status = persistence.getDataAttribute(DA_Status, String.class);
        if (status.equals("verified")) {
            return "already verified";
        }
        persistence.setDataAttribute(DA_Status, "verified");
        communication.publishInternalChannel(VERIFY_CHANNEL, null);
        return "done";
    }
}
```

## Example
- https://github.com/indeedeng/iwf/wiki/Use-case-study-%E2%80%90%E2%80%90-Microservice-Orchestration

## Link:
- [Dashboard](http://localhost:8233/namespaces/default/workflows)
- [IWF project](https://github.com/indeedeng/iwf)
- [Temporal](https://github.com/temporalio/temporal)
- [Cadence](https://github.com/uber/cadence)
- [Setup](https://github.com/indeedeng/iwf/blob/main/CONTRIBUTING.md#prepare-cadencetemporal-environment)

## temporal cli commands for registering attributes
```
temporal operator search-attribute create -name current_step -type Text
temporal operator search-attribute create -name aadhaarId -type Text
temporal operator search-attribute create -name aadhaar_id -type Text
temporal operator search-attribute create -name parentWorkflowId -type Text
temporal operator search-attribute create -name customer_id -type Text

temporal operator search-attribute create -name IwfWorkflowType -type Keyword
temporal operator search-attribute create -name IwfGlobalWorkflowVersion -type Int
temporal operator search-attribute create -name IwfExecutingStateIds -type Keyword
```

## List
```
temporal operator search-attribute list
temporal workflow list
```