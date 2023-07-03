# Guidelines for container based Java applications

## A - Java Framework 
The use case lambda functions are implemented using a combination of Java 8,Java 11 and the AWS SDK.

During the migration we have:
  1) Migrated the codebase of the services to Quarkus Java containers on Openshift Azure and we are using the Azure SDK and Java 11.
  2) For the base Java Framework we are using [Red Hat Quarkus Build](https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus) using https://code.quarkus.redhat.com/ libraries from the framework
  3) Azure SDK Asynchronous Programming (https://docs.microsoft.com/en-us/azure/developer/java/sdk/async-programming)

Criteria:
1. Designed and built exclusively for cloud native development, with reduced memory consumption and startup time.
2. Support for container functionality e.g. health checks, metrics, configuration, secrets & serverless functions.
3. Support for asynchronous programming model which is the Microsoft recommended approach for scalable, resource efficient services[4]. 
4. Automatic generation of K8s/OpenShift recource definitions.
5. Support for native binary generation for even further reduction of CPU/Memory usage.
6. Support for external systems under consideration e.g. HashiCorp Vault, Kafka.
7. Enhanced developer experience with dynamic reload and devloper services using test containers.
8. Fully supported by Red Hat.

### Reference Library
* https://developers.redhat.com/e-books
* https://developers.redhat.com/learn
* https://azure.microsoft.com/en-us/blog/free-ebook-the-developer-s-guide-to-microsoft-azure-now-available/
* https://azure.microsoft.com/en-us/resources/developer-s-guide-to-getting-started-with-azure-cosmos-db/
* https://azure.microsoft.com/en-us/resources/azure-cosmos-db-onboarding-best-practices/
* https://docs.microsoft.com/en-us/azure/cosmos-db/automated-recommendations (edited) 

### A1 - Application configuration

#### Configuration options
1. Quarkus Secrets & Configuration (https://quarkus.io/guides/kubernetes-config)
2. Environment Variables
3. Java System Properties

Further Documentation:
1. [ConfigMaps](https://docs.openshift.com/container-platform/4.11/nodes/pods/nodes-pods-configmaps.html)
2. [Secrets](https://docs.openshift.com/container-platform/4.11/nodes/pods/nodes-pods-secrets.html)
3. [Quarkus config map reload documentation](https://github.com/quarkusio/quarkus/discussions/23133)

:bangbang: Decision: **Quarkus Secrets will be loaded via Kubernetes API**

#### Implementation - See *[Use Plain K8s ConfigMap for passing non-sensitive configuration information](configs-handling.md)*
* Non-sensitive application configuration will be provided via a combination of kubernetes `ConfigMaps`[(1)](https://docs.openshift.com/container-platform/4.11/nodes/pods/nodes-pods-configmaps.html) and `environment` variables.
  * During development stages configs are stored in `src/main/resources/application.properties` (with `%dev`, `%test`, `%prod` prefix depending on the target profile)
  * In higher environments move them to a `ConfigMap` OCP Resource for deployment in higher environments.
* While a config map change can be propagated to the `POD` it is the responsibility of the pod to detect this change and reload, this is not performed automatically.
* **For implementation follow:** [Handling non-Sensitive Configs with `ConfigMap`](configs-handling.md)


#### Implementation - See *[Use Plain K8s secrets for passing sensitive information](secrets-handling.md)*
* Sensitive configurations such as keys, credentials etc. will be done via Kubernetes `Secrets`[(2)](https://docs.openshift.com/container-platform/4.11/nodes/pods/nodes-pods-secrets.html).
    * During development stages configs are stored in `src/main/resources/application.properties`  (with `%dev`, `%test`, `%prod` prefix depending on the target profile)
    * In higher environments move them to `Secret` OCP Resource for deployment in higher environments.
* **For implementation follow:** [Handling Sensitive Configs with `Secret`](secrets-handling.md)


### A2 - Application metrics

:bangbang: Decision: **Prometheus Metrics on Quarkus** 

- By default OpenShift gathers metrics by utilising the [Prometheus] (https://prometheus.io/) project. Prometheus is used to gather cluster metrics and can also be configured to gather Application workload metrics as long as the application exposes metrics   
via a prometheus metrics scraping endpoint. Prometheus is extensible to allow custom metrics to be easily retrieved and stored.
- In `Development` Namespaces we utilize Openshift ServiceMesh observability stack for the gathering of metrics, tracing, grafana dashboards and ui (via kiali) visualization
- In higher (`test`, `preprod`, `prod`) environments we utilize Datadog integrations to publish metrics 
- See [How to report custom business metrics on Datadog](datadog-app-metrics.md)
- See [How to Observe your service with Service Mesh Tools](https://kiali.io/docs/tutorials/travels/04-observe/)

#### Metrics Implementation Resources
1. Prometheus Client Libraries (https://prometheus.io/docs/instrumenting/clientlibs/)
2. Prometheus Metrics on Quarkus (https://quarkus.io/blog/micrometer-prometheus-openshift/)
3. Metrics instrumentation (https://micrometer.io/)
4. Quarkus Micrometer (https://quarkus.io/guides/micrometer)

#### Counter Metric Implementation

For an actual implementation look at [HelloCosmosResource.java](../quickstart-lambda-to-quarkus-rest/src/main/java/com/redhat/cloudnative/hellocosmos/HelloCosmosResource.java) 

* Step 1: Include Quarkus [Micrometer dependency](https://quarkus.io/guides/micrometer)

```XML
   <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
    </dependency>
```
* Step 2: Create Country Metric
```JAVA
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.scheduler.Scheduled;

  @Scheduled(every = "60s")
  public void pollScheduler() {
    try {
      registry.counter("poll_triggered", Tags.of("integration", "dmd")).increment();        
      }
  }
```

#### Other Type Metric Implementation
- RATE
- GAUGE
- SET
- HISTOGRAM
- DISTRIBUTION

### A3 - Application Trace Implementation


#### Trace Implementation

* Service Mesh based network traces captured and forwarded by `istio-proxy` (for production setup see [Red Hat Openshift Service Mesh ebook](https://www.redhat.com/en/resources/getting-started-with-openshift-service-mesh-ebook)) 
* Applications to generate own additional traces

## B - Package and Deploy

### B1 - Java runtime base image & Docker Build
A base image is the container image onto which the application is delivered to OpenShift for execution.

:bangbang: Decision: **Red Hat OpenJDK 11 base image**

Options:   
1. Self-build JVM image
2. Community image
3. Red Hat Build of Quarkus Image
4. Red Hat OpenJDK 11 base image (`registry.access.redhat.com/ubi8/openjdk-11:1.11`) 

Red Hat recommends using the Red Hat build of [Quarkus](https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus) as a base container image which is fully supported by Red Hat. 
This image can be used to build and deploy Java applications in a JVM or native-build mode. 

Red Hat strongly discourages the usage of self-built or non-verified community images due to security risks.
An intermediate step would be to use the Red Hat OpenJDK 11 base images.

#### `Dockerfile` for build

Each application will define a `Dockerfile` (see an example: [Rest quickstart Dockerfile](../quickstart-lambda-to-quarkus-rest/Dockerfile)) which will be used to place on top of the `registry.access.redhat.com/ubi8/openjdk-11:1.11` base image the application binaries and configurations required.
1. for local docker based builds and testing see (see: [Guidelines for building application and any external dependency containers for local functional tests](local-container-building-fort-testing.md))
2. for the pipeline based build during `Continuous Integration (CI)`  see :warning: TO BE DEFINED


### B2 - Deployment Types

For the containerized Application Deployment the following deployment types can be utilized

#### Kubernetes Deployment

For long running applications use this Kubernetes Deployment type (see example in [deployment.yaml](../quickstart-lambda-to-quarkus-rest/chart/templates/deployment.yaml))
* Ensure there is 
  * a `ServiceAccountName`
  * `probes` (see below)
  * `resources` configuration (see below)  
  * `labels` configuration (see below)
  * `annotations` configuration including for metrics/logging (see below)    

#### Kubernetes CronJob

For a Job type application that needs to start at pre-determined intervals, run and shutdown use this type and see examples in (see example [cronjob.yaml](../quickstart-lambda-to-quarkus-cronjob/chart/templates/cronjob.yaml)
* Ensure there is 
  * a `ServiceAccountName`
  * `probes` (see below)
  * `resources` configuration (see below)  
  * `labels` configuration (see below)
  * `annotations` configuration including for metrics/logging (see below)   



### B3 - Complete Deployment Contents 
#### Probes

Every traffic handling application running in a container must provide endpoints for kubernetes `readiness` and `liveness` probes(1). 
Non-traffic handling components e.g. `Jobs` don't need `readiness` probes but do need `liveness `probes.

`Startup` probes only are required for slow starting containers. They provide a way to defer the execution of `liveness` and `readiness` probes until a container indicates it’s able to handle them.  
Kubernetes won’t direct the other probe types to a container if it has a `startup` probe that hasn’t yet succeeded. It aims to reduce superfluous load on the cluster by blocking the execution of `readiness`/`liveness` probes until the application is capable of servicing these probes.

>**Important:** Ensure you configure according to your application's behavior the timings of the healthchecks.

See :
1. [Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
2. [Probes example](https://developers.redhat.com/blog/2020/11/10/you-probably-need-liveness-and-readiness-probes#what_about_identical_liveness_and_a_readiness_probes_)

##### Implementation
1. [SmallRye Health](https://github.com/smallrye/smallrye-health) implementation of the [Microprofile Health] specification (https://microprofile.io/project/eclipse/microprofile-health)
2. [Quarkus Health](https://quarkus.io/guides/smallrye-health)
3. [SpringBoot Actuator](https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/reference/html/production-ready-features.html#production-ready-kubernetes-probes)


#### Resource management

At its core Kubernetes is concerned about managing resources, primarily CPU and memory so that it can assign pods to execute on worker nodes. 
To help with scheduling Pods can specify their CPU and memory needs via resource requests and limits.  
The combination of Requests & Limits is key to optimizing the performance of the container platform and ensuring that Pods are not arbitrarily terminated when resources are under pressure.  
At a minimum CPU and Memory requests should be configured for pods, eg:

```
      containers:
        - resources:
            limits:
              cpu: 400m
              memory: 1Gi
            requests:
              cpu: 200m
              memory: 512Mi
```              

See:
1. Kubernetes Resource Management (https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)

#### Container Labels & Annotations
##### Labels

Labels are an important concept in Kubernetes. Labels along with selector can be used for workload placement, affinity, service routing etc (1).  
Each application must use, at a minimum, the default set of kubernetes labels (2). Additional labels can be added as required. 

**Note:** Be aware of kubernetes naming constraints when defining labels https://kubernetes.io/docs/concepts/overview/working-with-objects/names/

See :
1. [Labels] (https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)
2. [Default Labels] (https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels/)

##### Annotations
* Annotations are a method to attach non-Kubernetes specific data e.g. metadata to kubernetes resources.  
* This approach can often be used by Operators to drive functionality e.g.Service Mesh annotations (3).  
* Annotation definition and usage are at the discretion of the developer but care should be taken not to use annotations used by the OpenShift platform or 3rd party components  
e.g. "openshift.io/key:value" or "*.istio.io/key:value

###### Implementation of Service Mesh Annotations


* Include an application in the mesh by adding the annotation in the relevant container's `metadata` section of the `deployment.yaml`. [Service Mesh pod annotations] (https://docs.openshift.com/container-platform/4.10/service_mesh/v2x/prepare-to-deploy-applications-ossm.html)

```YAML
sidecar.istio.io/inject: '{{ .Values.service_mesh_enabled }}'
```

###### Implementation of Exposing to Datadog Annotations 

* See [Datadog - Report custom business metrics](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/datadog-app-metrics.md)
* See [Datadog - Forward application logs](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/datadog-log-forwarding.md)

### B4 -Service

 * For the exposure of an Application's endpoints to client services the creation of a [`Service`](https://docs.openshift.com/online/pro/architecture/core_concepts/pods_and_services.html#services) is required and an example can be found at [hello-cosmos/service.yaml](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/chart/templates/service.yml)






