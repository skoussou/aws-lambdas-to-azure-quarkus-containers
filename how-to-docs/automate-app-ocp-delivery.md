# Automate delivery of new Service in OCP environments      

This document contains the path to automate the application delivery

## Promotion Namespaces 

    "Application promotion means moving an application through various runtime environments, typically with an increasing level of maturity. 
    For example, an application might start out in a development environment, then be promoted to a stage environment for further testing, 
    before finally being promoted into a production environment. As changes are introduced in the application, again the changes will start 
    in development and be promoted through stage and production."*
    

The expectation is that the following environments will be made available

* **DEV Environment**: A namespace per application (application can be defined as all related components delivering domain functionality -eg. registering and pairing- which may be split into more namespaces in higher environment). The intention of this environment is to test and debug the application in isolation with all integrations mocked. There are 2 options on how to deploy into this environment: 
  * **Options 1:** The expectation is that this will be a playground for developers to get their application code into a working and deployable state by [migrating code to quarkus](migrate-business-application-code-in-quarkus.md), [creating helm charts](helm-chart-for-app-ocp-deployment.md) and [containerizing the app](container-guidelines.md). The developers have access via Openshift Developer console and `oc` CLI.
  * **Options 2:** Alternatively, each application will have a *secondary pipeline* to deliver experimental code and OCP resources in the environment namespace. 
* **TEST Environment**: Traditionally called "IOT" in WirelessCar. The environment where initial functional tests take place against acceptance criteria before moving to higher environments. All integrations between applications should be functional but the system-external inetgrations should be mocked. In this environment namespaces will be as in their final format.Deployment should exclusively be done through GitOps.
* **PRE-PROD Environment**: This environment is for testing integrations towards external systems such as those owned by the customer or third parties. Deployment should exclusively be done through GitOps.
* **PROD Environment**: The production environment - i.e. where end-customer affecting workloads are done. Will probably be housed on a different OpenShift/Kubernetes cluster than the non-production environments - this is an open question. Deployment should exclusively be done through GitOps.

The documentation [Promoting Applications Across Environments](https://docs.openshift.com/online/pro/dev_guide/application_lifecycle/promoting_applications.html) offers further understanding of the topic in an Openshift environment.

## Create new Service Pipeline for the Service to perform `Continuous Integration`
We are using [OpenShift Pipelines (Tekton)](https://docs.openshift.com/container-platform/4.7/cicd/pipelines/creating-applications-with-cicd-pipelines.html) as our CI solution. 

There are ready to use pipelines that can help you to build your app and deploy on OpenShift. They are located in [helm-chart repository](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-maven-pipeline#using-this-pipeline).

* **Maven Pipeline**: It takes your source code, tests it, containerize it and deploy into an environment on OpenShift. The pipeline itself is generic and works for any maven project. You can deploy the pipeline in your `ci-cd` environment and start using it for all of your maven project. For that, please follow this documentation: [how to use Maven pipeline](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-maven-pipeline#using-this-pipeline) 

* **NGINX Pipeline**: It builds an NGINX application, bake it into a container image and publish it to the Azure container repository. You can add it to `ci-cd` namespace as described [here](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-nginx-pipeline#using-this-pipeline)

* **Java Library Pipeline**: It helps you to set up a Tekton pipeline to build a Java Library application, and publish it to a Github repository ready for use by deployable services.You can add it to `ci-cd` namespace as described [here](https://github.com/cariad-cloud/residency-helm-charts/tree/main/residency-java-library-pipeline#using-this-pipeline)


In case this generic pipeline does not suit your needs, you can create a new pipeline. For that; 
* [Creating a new or re-using an existing pipeline documentation](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/create-new-cicd-pipeline.md)  can help you. 

All these pipelines give you an endpoint URL to add your source code repository as a webhook so that everytime you push your repo, your pipeline is triggered automatically.
## Create GitOps Resources for the new Service to perform `Continuous Delivery`
Continuous Delivery requires GitOps Engine and other resources like namespaces, secrets or our own application definitions in place. In order to do that we need ArgoCD as GitOps Engine and ArgoCD Application definition that do the creation of all the necessary objects for us.
In [this documentation](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/setup-gitops-service.md#setting-up-gitops-with-argocd-for-serviceteam) you'll find how to get an ArgoCD for yourself.

In the following section on the same documentation, you get environments (namespaces) for your application to run. Please see [this section](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/setup-gitops-service.md#6-set-up-gitops-repository-contents) of the same doc. 

You'll see below as an example:

```yaml
dev_namespace: &dev "banana-dev"
test_namespace: &test "banana-test"
preprod_namespace: &preprod "banana-preprod"
prod_namespace: &prod "banana-prod"
```

You can define the environment (namespaces) you need here for your services and ArgoCD will create it for you.

If you go to [next section](https://github.com/cariad-cloud/residency-docs/blob/main/how-to-docs/setup-gitops-service.md#9-adding-an-application-deployment), you'll find how to define your applications to be deployed on OpenShift into those namespaces via GitOps approach.

When you have the necessary definitions in place, pipeline will take care of reflecting your newly build image versions into your GitOps repository..which ArgoCD will see these changes and happily rollout them into your cluster! Congrats, that's how Continuous Delivery is done! 🎉