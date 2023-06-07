# Creating a pipeline to deploy my new Service

Note: In [residency-helm-charts](https://github.com/cariad-cloud/residency-helm-charts) you will find existing pipelines (eg. [residency-maven-pipeline](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-maven-pipeline), [residency-nginx-pipeline](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-nginx-pipeline)) which you can reuse by following [README.md](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-maven-pipeline/README.md)


## Pre-requisite

When done with the creation of a new pipeline or if you re-use an existing ensure you add your new pipeline application to the main ["App of Apps" list for ArgoCD](https://github.com/cariad-cloud/residency-gitops/blob/main/tooling/values-tooling.yaml). eg.

```YAML
  # Maven Pipeline - SIM
  - name: bom-pom
    enabled: true
    source: https://github.com/cariad-cloud/residency-bom
    source_path: residency-deploy-only-pipeline/
    values:
      name: sim-pipeline
      service_name: sim 
```      


## Create a New Pipeline

### Copy one of the existing pipelines 

eg.

```BASH
cp -r residency-maven-pipeline residency-deploy-only-pipeline
```

### Modify the pipeline content 

- `Chart.yaml`: name of the chart, version etc.
- `values.yaml`: The values that are needed by the pipeline (if the pipeline is generic these will be injecting by ArgoCD in `residency-gitops` repository as the `Applicaiton` eployment is created see above)
- `templates/pipelines/maven-pipeline.yaml`: The pipeline steps
- `templates/pipelines/config-maven-settings.yaml`: If the pipeline contains maven tasks these are the settings for it
- `templates/secrets/serviceaccount.yaml`: secrets, roles, rolebindings. serviceaccount for the pipeline tasks
- `templates/triggers/*`: Triggering pipeline from a github repository

### Modify the triggering pipeline content 

:warning: *TO BE COMPLETE*

- `event-listener.yaml`:
- `maven-trigger-template.yaml`:
- `trigger-binding.yaml`:

### Retrieve webhook and apply in gitub repository

* Get the URL of the pipeline webhook:
  * Under `labs-ci-cd` namespace -> `Networking` -> `Routes` find  <service-name>-webhook and get the `Location` URL
  * or `echo https://$(oc get route bom-pom-webhook -o jsonpath='{.spec.host}' -n labs-ci-cd)`
* Go to the github repository which will need to trigger the pipeline (`Settings` -> `Webhooks` -> `Add webhook`

```
Payload URL:	ROUTE URL
Content type:	application/json
```



