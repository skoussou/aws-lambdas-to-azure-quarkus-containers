# Use Plain K8s `ConfigMap` for passing non-sensitive configuration information

## Context and Problem Statement

Applications need access to configuration information such as urls and database names etc. 
We need to define a common reusable and simple approach for accessing this data within a pod.
K8s `configMap` can be passed into a container either as a file on a file system or via environment variables.

## Considered Options

* K8s `configMap` via files
* K8s confgis via environment variables

## Decision Outcome

Selected Option: K8s `configMap` via files mounted into TMPFS volumes and applications can be configured to read configs from these volumes. Kubernetes `Deployment` can mount these config properties files from a `configMap` into a running pod for the quarkus application to use as follows:

	kind: Deployment
	apiVersion: apps/v1
	metadata:
	  name: hello-cosmos
	  labels:
	    app: hello-cosmos
	    group: com.redhat.cloudnative
	    version: 1.0.0-SNAPSHOT
	spec:
	...
	  template: 
	...
	    spec:
	...
		  volumeMounts:
		      - mountPath: /deployments/config
		        name: application-properties
	...
	      volumes:
		- name: application-properties
		  secret:
		    secretName: application.properties

## To access the `configMap` keys in Quarkus 

* In your code perform the following replacing the `name` with your key

  ```Java
  @ConfigProperty(name = "cosmos.database")
    protected String databaseName;
  ```