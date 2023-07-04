# Cookbook for activities to migrate AWS Java Lambda applications to AZURE/OCP

## Prerequisites
- GitOps with ArgoCD for the service/team has been setup see [[TODO] - Setting up GitOps with ArgoCD for service/team](TODO)
- Red Hat OpenShift Pipelines operator has been installed
- Red Hat OpenShift Service Mesh operator has been installed, `ServiceMeshControlPlane` configured, `ServiceMeshMemberRole` configured with the namespace (see [[TODO] - Service Mesh Setup](TODO)) 
- Datadog [[TODO] - has been installed](TODO) and configured to have access to this environment
- *(Optional)* Kafka Operator has been installed and Kafka instance, Kafka Clients, Kafka Topics have been configured [Kafka prerequisites](quickstart-lambda-to-quarkus-kafka/README.md#prerequisites)
- *(Optional)* Crossplane for database creation is in place and `master_key` and `connection` URL known

## Assumptions
For this guide, we will assume that your Lambda service is called `banana` to make the examples simple.

## `Banana` Lambda Migration Flow

### High Level Migration Flow

![High Level Migration FLow](images/HighLevelMigrationFlow-Lamda-to-Quarkus.png)

### Detailed Migration Flow

```mermaid
graph TB

  subgraph "Promote Applications Across Environments"
  Node5a[Promotion Namespaces Definition]
  Node5a -- Optional --> Node5b[Setup ArgoCD Application for CI Pipeline]
  Node5a --> Node5c[Setup ArgoCD Application for GitOps]
  end

  subgraph "Prepare Service for OCP Automated delivery via DevOps Tooling"
  Node2b[Create Helm Chart]
  Node2b[Create Helm Charts] --> Node2c[create keyvault content]
  Node2b[Create Helm charts] --> Node2f[values.yaml]
  Node2f[values.yaml] -- optional --> Node2r[cosmosdb-claim.yaml]
  Node2f[values.yaml] -- optional --> Node2s[kafka-topic.yaml]
  Node2f[values.yaml] -- optional --> Node2d[kafka-user.yaml]
  Node2f[values.yaml] --> Node2g[configmap.yaml]
  Node2g[configmap.yaml] --> Node2h[externalsecret.yaml]
  Node2h[externalsecret.yaml] -- Expectation Someone to Generate the Vault Content --> Node2c[create keyvault content]
  Node2h[externalsecret.yaml] --> Node2i[roles.yaml]
  Node2i[roles.yaml] --> Node2j[roleBinding.yaml]
  Node2j[roleBinding.yaml] --> Node2k[serviceAccount.yaml]
  Node2k -- either  --> Node2l[deployment.yaml]
  Node2k -- or --> Node2q[cronjob.yaml]
  Node2l[deployment.yaml] --> Node2m[service.yaml]
  Node2m -- either via servicemesh --> Node2n[istio-virtualservice.yaml]
  Node2m -- or --> Node2o[route.yaml]
  Node2m -- or --> Node2p[NO EXTERNAL ACCESS]
  Node2n -- Resources Ready --> Node5a
  Node2o -- Resources Ready --> Node5a
  Node2p -- Resources Ready --> Node5a
  end

  subgraph "Activities to Setup and Integrate with External Dependencies"
  Node3a[External Dependencies]
  Node3a[External Dependencies] --> Node3b[Integrating with CosmosDB]
  Node3a[External Dependencies] --> Node3c[Integrating with Kafka]
  Node3b[Integrating with CosmosDB] -- Code Ready --> Node5a
  Node3c[Integrating with Kafka]  -- Code Ready --> Node5a
  click Node3b "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/-/tree/main/quickstart-lambda-to-quarkus-rest#code-migrations" "CosmosDB"
  click Node3c "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/blob/main/quickstart-lambda-to-quarkus-kafka/README.md#code-migrations" "Kafka"
  end

  subgraph "Migrate new Service Code from Lambda to Quarkus"
  Node1a[Setup Application Configurations]
  Node1a -- feed configs into --> Node2g[configmap.yaml]
  Node1a -- feed sensitive configs into --> Node2h[externalsecret.yaml]
  Node1b[Setup Application Metrics]
  Node1a --> Node1b[Setup Application Metrics]
  Node1b -- Option-1 --> Node1c[Migrating code into CronJob Quarkus Application Main]
  Node1b -- Option-2 --> Node1d[Migrating code into Long-running REST API Quarkus ApplicationScoped]
  Node1b -- Option-3 --> Node1e[Migrating code into Quarkus Scheduled]
  Node1b -- Option-4 --> Node1f[Migrating code into Kafka Triggered Quarkus]
  Node1c -- Optional --> Node3a[External Dependencies]
  Node1d -- Optional --> Node3a[External Dependencies]
  Node1e -- Optional --> Node3a[External Dependencies]
  Node1f -- Optional --> Node3a[External Dependencies]
  click Node1a "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/blob/main/how-to-docs/container-guidelines.md#a1---application-configuration" "App Configuration"
  click Node1b "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/blob/main/how-to-docs/container-guidelines.md#a2---application-metrics" "Setup Application Metrics"
  click Node1c "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/tree/main/quickstart-lambda-to-quarkus-cronjob#migrating-a-job-based-lambda-to-quarkus-application" "Start Migrating a CronJob"
  click Node1d "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/tree/main/quickstart-lambda-to-quarkus-rest#migrating-a-rest-api-based-lambda-to-quarkus-application" "Start Migrating a Rest Service"
  click Node1e "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/tree/main/quickstart-lambda-to-quarkus-scheduled#migrating-a-scheduled-based-repeatable-running-lambda-to-quarkus-application" "Start Migrating a Scheduled Service"
  click Node1f "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/blob/main/quickstart-lambda-to-quarkus-kafka/README.md#migrating-a-message-triggered-long-running-lambda-to-quarkus-application" "Start Migrating a Message Triggered Service"
  end

  subgraph "Migrating an AWS Lambda Service to Quarkus Container Service"
  Node1[Start Migrating a Service] --> Node2[Choose a quickstart/skeleton quarkus service]
  Node2[Choose a quickstart/skeleton quarkus service] --> Node3[Containerize the application]
  Node3[Containerize the application] -- Path: prepare for deployment --> Node2b[Create Helm Chart]
  Node3[Containerize the application] -- Path: prepare business code --> Node1a[Setup Application Configurations]
  click Node2 "https://github.com/skoussou/aws-lambdas-to-azure-quarkus-containers/blob/main/how-to-docs/lambda-to-quarkus-quickstarts.md#list-of-quickstarts-for-the-migration-of-lambdas-to-quarkus" "Choose Quickstart"

end
```






