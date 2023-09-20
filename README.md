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

## [Basic Concepts](https://github.com/indeedeng/iwf/wiki/Basic-concepts-overview)

### RPC: API for application to interact with the workflow. It can access to persistence, internal channel, and state execution
- [RPC](https://github.com/indeedeng/iwf/wiki/RPC#signal-channel-vs-rpc)
- [RPC vs Signal](https://github.com/indeedeng/iwf/wiki/RPC#signal-channel-vs-rpc)
  - RPC + Internal Channel = Signal Channel
  - Inter Channel and Signal Channel are both message queues
  - RPC is synchronous API call [Definition](https://github.com/indeedeng/iwf-java-sdk/blob/main/src/main/java/io/iworkflow/core/RpcDefinitions.java)
  - Signal channel is Asynchronous API call

### Determinism and Versioning
- [IWF doc](https://github.com/indeedeng/iwf/wiki/Compare-with-Cadence-Temporal#determinism-and-versioning)
- Use flag to control the code execution as versioning in removed
- Since there is no versioning non-determinism issue will not happen 

### Java workflow definition
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
        if (status == "verified") {
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
- 