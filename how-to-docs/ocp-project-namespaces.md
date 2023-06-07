# OpenShift Project setup
https://docs.openshift.com/container-platform/4.10/applications/projects/configuring-project-creation.html

OpenShift projects are an abstraction built ontop of Kubernetes Namespaces.
This ADR will outline a logical definition of what projects will be used as part of the residency

## Layout 
For the residency we will use four main logical projects - dev, test, preprod and prod.
The purpose for each project is as follows

### Dev
Shared environment used for experimentation at a team level. Multiple versions of services can deployed manually or via pipelines.
Stubs/Mocks for external services. 

### Test
Controlled environment used for testing at a team level. All artefacts are built using CI and deployed via gitops pipelines. 

### PreProduction
Controlled environment. All artefacts are built using CI and deployed via gitops pipelines

### Production
Controlled environment. All artefacts are built using CI and deployed via gitops pipelines.