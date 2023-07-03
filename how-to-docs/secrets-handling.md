# Use Plain K8s secrets for passing sensitive information

## Context and Problem Statement

Applications need access to sensitive information such as keys and credentials. 
We need to define a common reusable and simple approach for accessing this data within a pod.
K8s secrets can be passed into a container either as a file on a file system or via environment variables.

## Considered Options

* K8s secrets via files
* K8s Secrets API
* K8s secrets via environment variables
* 3rd party vendor API

## Decision Outcome

Chosen option: K8s Secrets API
* Use [Kubernetes API](https://quarkus.io/guides/kubernetes-config) to read the secret or microprofile.
  * How To:
    * Use a `ServiceAccount` in the `Deployment` , used to run the application, with permissions for access to view the secret.
        ```YAML
        kind: ServiceAccount
        apiVersion: v1
        metadata:
          name: quarkus-secret-example
          namespace: lambdas-playground  
        ```    
      * Rolebindings
        ```YAML
        kind: RoleBinding
        apiVersion: rbac.authorization.k8s.io/v1
        metadata:
          name: quarkus-secret-keys-view
          namespace: lambdas-playground
        subjects:
          - kind: ServiceAccount
            name: quarkus-secret-keys
        roleRef:
          apiGroup: rbac.authorization.k8s.io
          kind: ClusterRole
          name: view   
        ---
        kind: RoleBinding
        apiVersion: rbac.authorization.k8s.io/v1
        metadata:
          name: quarkus-secret-keys-view-secrets
          namespace: lambdas-playground
        subjects:
          - kind: ServiceAccount
            name: quarkus-secret-keys
        roleRef:
          apiGroup: rbac.authorization.k8s.io
          kind: Role
          name: view-secrets
        ```
        * Roles
        ```YAML
        kind: Role
        apiVersion: rbac.authorization.k8s.io/v1
        metadata:
          name: view-secrets
          namespace: labs-test
        rules:
          - verbs:
              - get
            apiGroups:
              - ''
            resources:
              - secrets
        ```
    * Add in the service code repository
      * Add in `pom.xml` the depenency
        ```XML
        <dependency>
         <groupId>io.quarkus</groupId>
         <artifactId>quarkus-kubernetes-config</artifactId>
        </dependency>      
        ```    
      * In `application.properties` define the secret to use (only in `prod` profile)
        ```properties
        # Kubernetes Secret
        %prod.quarkus.kubernetes-config.secrets.enabled=true
        %prod.quarkus.kubernetes-config.secrets=db-credentials
      * In the Java Class define key to use from the secret eg.
        ```Java
            @ConfigProperty(name = "key")
            protected String value;
        ```
  * Pros: 
    * non-visible from outside the POD deployment or deployment configs
    * works out-of-the-box
  * Negatives:
    * does not obfuscate when logging the secret content
    * [Kubernetes Permissions](https://quarkus.io/guides/kubernetes-config#kubernetes-permissions) required for the application `ServiceAccount` (_Since reading ConfigMaps involves interacting with the KUBE API_)
    




