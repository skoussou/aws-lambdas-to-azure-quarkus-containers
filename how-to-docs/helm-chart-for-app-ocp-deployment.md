# Helm charts for OCP Resources to deploy the application

This document describes the helm chart creation and resources to be configured by the `banana` service developers, as required by the application code, so that the chart will facilitate their deployment and that of the application in Openshift.

## Assumptions

You have selected one of the proposed [residency quickstarts](residency-quickstarts.md) which contains an example of **chart** folder

## Create Helm Chart

Firstly, update the contents of the `Chart.yaml` file setting the `description`, `name`, `version`, `home` to values appropriate for your project.

### `values.yaml` Resource Configuration Guidelines

* Filename: `values.yaml`
* The `values.yaml` will contain values used to parameterize all the Openshift Resources created in the following sections. 
* *Important:* if these are changing values per environment of deployment (eg. dev, test, preprod, prod) these values will be overwritten (per environment) when creating an `ArgoCD GitOps application`.

### `ConfigMap` (`configmap.yaml`) Resource Configuration Guidelines

* Filename: `configmap.yaml`
* [`ConfigMap`](https://docs.openshift.com/online/pro/dev_guide/configmaps.html) is an Openshift/Kubernetes resource for storing **non-sensitive** application configurations. It is important as the developer of service `banana` to extract the quarkus configuration properties (under `src/main/resources/application.properties`) required during Openshift deployment and stores them in a `ConfigMap`.
* Follow: [Handling non-Sensitive Configs with `ConfigMap`](configs-handling.md)

### `ExternalSecret` (`externalsecret.yaml`) Resource Configuration Guidelines

* Filename: `externalsecret.yaml`
* Sensitive configurations will be provided as Openshift `Secrets` Read [How To - Use Plain K8s secrets for passing sensitive information](secrets-solution.md)
* In advance of creating an `externalsecret.yaml` the secret value must be stored in a `KeyVault`
  * In order to create a KeyVault, use this [helm chart](https://github.com/cariad-cloud/residency-helm-charts/tree/main/externalsecrets) as described in its README.
  Now that you have a KeyVault on Azure, you can add your secrets, certificates or keys.
  * The same helm chart also gives you a [Secret Store](https://external-secrets.io/v0.5.7/provider-azure-key-vault/) definition which helps you to retrieve the secrets from your Key Vault. 
  By refering that Secret Store, you can safely store your secret definitions in your Git repos.

* The `External Secrets Operator` is installed and the developer of `banana` application must provide 
  * a definition of `externalsecret.yaml` and
  * `values.yaml` with the values of the `keyvaultname`, `secret` (to be created) name and `key` the sensitive value to be saved in the `secret`
  * An example of the above with multiple `externalsecret.yaml` created by helm can be seen  below
    * `externalsecret.yaml` template
      ```YAML
      {{- range $key := .Values.secrets }}
      apiVersion: external-secrets.io/v1beta1
      kind: ExternalSecret
      metadata:
        name: {{ .secret }}
      spec:
        data:
          - remoteRef:
              conversionStrategy: Default
              key: {{ .secret }}
            secretKey: {{ .key }}
        refreshInterval: 1h
        secretStoreRef:
          kind: SecretStore
          name: secret-store-sim-reg-test-keyvault
        target:
          creationPolicy: Owner
          deletionPolicy: Retain
          name: {{ .secret }}
      {{- end }}
      ```
    * `values.yaml` multiple keyvault, secret name and key values
    ```YAML
      secrets:
        m2mactorsecret:
          k8s:
            secret: sim-reg-m2m-actor-secret
            key: sim-m2m-actor-secret        
        database: residency-sim-data-handler
        container: tokencache
        partitionkey: clientId
    ```
  * Additional examples based on single resource creation can be seen at [sim-changeid-cosmos-master-key-externalsecret.yaml](https://github.com/cariad-cloud/residency-sim-management-infrastructure/blob/main/chart/templates/sim-changeid-cosmos-master-key-externalsecret.yaml), [values.yaml](https://github.com/cariad-cloud/residency-sim-management-infrastructure/blob/2b0b6c991dea6f798a752c1e876adbee553320df/chart/values.yaml#L10)


:warning: An exception may occur (in the current implementations) if getting secret via the `keyvault` is not viable (eg. file based secrets for certificates). We have opted during the residency for the direct creation of `Secret` resources (eg. KAFKA) from the helm chart and not via the *External Secrets* but as this is a headache for maintenance Issue [Secrets based on files are not stored in keyvault](https://github.com/cariad-cloud/residency-docs/issues/26) has been raised to review and rectify.  

### `Role` (`roles.yaml`) Resource Configuration Guidelines

Certain roles (eg. for the purpose of reading `Secret` via Openshift API) needs to be made available in the namespace.

* Filename: `roles.yaml` ([example](https://github.com/cariad-cloud/residency-sim-data-handler/blob/main/chart/templates/roles.yaml))

### `RoleBinding` (`roleBinding.yaml`) Resource Configuration Guidelines

Certain roles (eg. for the purpose of reading `Secret` via Openshift API) need to be associated with a `ServiceAccount` used in the deployment of an Applicaiton POD.

* Filename: `roleBinding.yaml` ([example](https://github.com/cariad-cloud/residency-sim-data-handler/blob/main/chart/templates/roleBinding.yaml))

### `ServiceAccount` (`serviceAccount.yaml`) Resource Configuration Guidelines

The `ServiceAccountName` under which the application POD will be started

* Filename: serviceAccount.yaml ([example](https://github.com/cariad-cloud/residency-sim-data-handler/blob/main/chart/templates/serviceAccount.yaml))

### `DocumentDatabase` (`cosmosdb-claim.yaml`) Resource Configuration Guidelines

Determines the configuration of the Cosmos DB applied by `crossplane` operator to Azure once the resource is in place. 

* Filename: `cosmosdb-claim.yaml`
* Examples:
  * [hello-cosmos cosmosdb-claim.yaml](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/chart/templates/cosmosdb-claim.yaml)
  * [residency-token-horder cosmosdb-claim.yaml](https://github.com/cariad-cloud/residency-token-hoarder/blob/main/chart/templates/cosmosdb-claim.yaml)

### `KafkaTopic` (`kafka-topic.yaml`) Resource Configuration Guidelines

* Filename: [`kafka-topic.yaml`](https://github.com/cariad-cloud/quickstart-residency-scheduled-quarkus/blob/main/chart/templates/kafka-topic.yaml)

this file is used in a working application and quick sstart here : [https://github.com/cariad-cloud/residency-kafka-quickstart#prerequisites](https://github.com/cariad-cloud/residency-kafka-quickstart#prerequisites)

### `KafkaUser` (`kafka-user.yaml`) Resource Configuration Guidelines

* Filename: [`kafka-user.yaml`](https://github.com/cariad-cloud/residency-kafka-quickstart/blob/main/chart/templates/quickstart-kafka-user.yaml)

this file is used in a working application and quick sstart here : [https://github.com/cariad-cloud/residency-kafka-quickstart#prerequisites](https://github.com/cariad-cloud/residency-kafka-quickstart#prerequisites)


### `Deployment` (`deployment.yaml`) Resource Configuration Guidelines

* Filename: `deployment.yaml`
* Follow the guidelines in [Guidelines for Kubernetes Deployment](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/container-guidelines.md#deployment) to update the selected `quickstart` for the purposes of your service

### `CronJob` (`cronjob.yaml`) Resource Configuration Guidelines

* Filename: `cronjob.yaml`
* Follow the guidelines in [Guidelines For CronJob Deployment](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/container-guidelines.md#kubernetes-cronjob) to update the selected `quickstart` for the purposes of your service

### `Service` (`service.yaml`) Resource Configuration Guidelines

* Filename: `service.yaml`
* Follow the guidelines in [Guidelines for Service](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/container-guidelines.md#service) to update the selected `quickstart` for the purposes of your service

### (Option 1) Istio `VirtualService` - For Service Mesh Based service - Resource Configuration Guidelines

If an application needs to be accessible beyond the Openshift cluster for ingress, and it is part of the Service Mesh.

* Filename: `istio-virtualservice.yaml`
* Follow the guidelines in [Guidelines for getting traffic into a Service Mesh based Service](servicemesh-routing-into.md) to update the selected `quickstart` for the purposes of your service.

### (Option 2) Openshift `Route` - For NON - Service Mesh Based service - Resource Configuration Guidelines

If an application needs to be accessible beyond the Openshift cluster for ingress, but it is part of the Service Mesh.

* Filename: `route.yaml`
* Follow the guidelines in [Guidelines for getting traffic into a Service Mesh based Service](https://docs.openshift.com/online/pro/dev_guide/routes.html) to update the selected `quickstart` for the purposes of your service.
