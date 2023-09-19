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
