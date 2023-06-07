# Setting up GitOps with ArgoCD for service/team

## Prerequisities
1. ArgoCD operator has been installed on the OpenShift Cluster

## Assumption
For this guide, we will assume that your new service is called `banana` to make the examples simple.

## 1. Create new gitops repository
Simply create a new repository in GitHub and share it with your team. A suggestion for naming is `<context>-<service/team>-gitops` - e.g. `residency-banana-gitops`. Keep it empty for now.

## 2. Create new CI/CD namespace
We must create namespaces to house the new CI/CD setup for the service/team. This can be done manually but preferably through a top-level ArgoCD. In the residency, we did this through the `residency-gitops` repository by using the `bootstrap-project` chart from the RedHat CoP repository.
```yaml
applications:
  # Bootstrap Project
  - name: bootstrap
    enabled: true
    source: https://redhat-cop.github.io/helm-charts
    chart_name: bootstrap-project
    source_ref: "1.0.1"
    values:
      namespaces:
        # ... Other namespaces omitted
        - name: "banana-ci-cd"
          bindings: *binds
          operatorgroup: false
```
Manual creation can be done like this with `oc` through your command line.
```bash
$ oc new-project banana-ci-cd
```

## 3. Manually set up git-auth secret in service cicd namespace and populate with git credentials.
This should contain two fields. `username` which is the username of the functional github account that should be used by ArgoCD, and `password` that should contain a personal access token for that user. See [the github documentation on how to create a token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).
```yaml
kind: Secret
apiVersion: v1
metadata:
  name: git-auth
data:
  username: yyy
  password: xxx
type: kubernetes.io/basic-auth
```

## 4. Deploy new ArgoCD in your new namespace
Now we are going to install a new ArgoCD instance in the new namespace that will be used to manage deployments for your service or team. 

The recommended way is to use the `gitops-operator` helm chart that is available here: https://github.com/redhat-cop/helm-charts/tree/master/charts/gitops-operator

When setting it up during the residency, we did this deployment through the "parent" ArgoCD, using the following definition.
```yaml
  # Service specific argoCD instances
  - name: service-argocds
    enabled: true
    source: https://redhat-cop.github.io/helm-charts
    chart_name: gitops-operator
    source_ref: "0.3.10"
    values:
      ignoreHelmHooks: false
      namespaces:
       - "banana-ci-cd" # This is what you would update with your new namespace
      secrets: ""
      argocd_cr:
        repositoryCredentials: |
          - url: https://github.com/cariad-cloud
            type: git
            passwordSecret:
              key: password
              name: git-auth
            usernameSecret:
              key: username
              name: git-auth
        resourceExclusions: |
          - apiGroups:
              - tekton.dev
            clusters:
              - '*'
            kinds:
              - TaskRun
              - PipelineRun
          - apiGroups:
              - 'runtime.cariad.net'
            clusters:
              - '*'
            kinds:
              - XDocumentDatabase
              - XKeyVault
      operator: ''
```

## 5. Set new ArgoCD to cluster-level ArgoCD
Per default an ArgoCD can only deploy and manage resources within its own namespace. But we want the new ArgoCD capable of creating new namespaces that it can manage. To do this, we must update the Operator subscription for the `openshift-gitops-operator` AKA "Red Hat OpenShift GitOps". This will require cluster admin access. The relevant configuration is under `spec.config.env[ARGOCD_CLUSTER_CONFIG_NAMESPACES]`
```yaml
spec:
  channel: latest
  config:
    env:
      - name: ARGOCD_CLUSTER_CONFIG_NAMESPACES
        value: 'banana-ci-cd, ...other namespaces'
```

## 6. Set up gitops repository contents.
Now we actually need to set up the gitops repository. We want a directory structure like this:
```
templates
    +---argo-application.yaml
tooling
    +---values.yaml
Chart.yaml
values.yaml
```
Following is all the files and their initial contents (remember to replace `banana`).

`Chart.yaml` - This declares the Helm chart. More properties are available if wanted, see https://helm.sh/docs/topics/charts/#the-chartyaml-file
```yaml
apiVersion: v2
name: banana-gitops
description: Gitops setup for Banana service/team
version: 1.0.0
```
`values.yaml` - These declare the input values for the helm chart. In this case it is used to set up the "App of apps" within ArgoCD that control the deployments.
```yaml
source: https://github.com/<github-org>/residency-banana-gitops.git
team: banana
release: ci-cd

applications:
# Will manage the CI/CD tooling
  - name: tooling-app-of-banana
    enabled: true
    source_path: "."
    helm_values:
      - tooling/values.yaml

## Will manage deployments of the actual applications
#  - name: test-app-of-banana
#    enabled: true
#    source_path: "."
#    helm_values:
#      - banana/test/values.yaml

#  - name: preprod-app-of-banana
#    enabled: true
#    source_path: "."
#    helm_values:
#      - banana/preprod/values.yaml

#  - name: prod-app-of-banana
#    enabled: true
#    source_path: "."
#    helm_values:
#      - banana/prod/values.yaml
```
`tooling/values.yaml` - Input values for tooling app of apps. Declares resources that are commonly used between the different deployments as well as the namespaces.
```yaml
dev_namespace: &dev "banana-dev"
test_namespace: &test "banana-test"
preprod_namespace: &preprod "banana-preprod"
prod_namespace: &prod "banana-prod"

# App of applications list
applications:
  # Bootstrap Project
  - name: bootstrap
    enabled: true
    source: https://redhat-cop.github.io/helm-charts
    chart_name: bootstrap-project
    source_ref: "1.0.1"
    values:
      bindings: &binds # this wirelesscar-developers is the GROUP NAME in IDM
        - name: wirelesscar-developers
          kind: Group
          role: edit
      namespaces:
        - name: *dev
          bindings: *binds
          operatorgroup: true
        - name: *test
          bindings: *binds
          operatorgroup: true
        - name: *test_nginx
          bindings: *binds
          operatorgroup: true
      serviceaccounts: ""

  # Tekton Maven Pipeline
  - name: banana-pipeline
    enabled: true
    source: https://github.com/cariad-cloud/residency-helm-charts.git
    source_path: residency-maven-pipeline/
    values:
      name: banana-pipeline
      service_name: banana
      gitops_repo: residency-banana-gitops

```
`templates/argo-application.yaml` - Heavily 'templated' abstraction ArgoCD application so that applications in ArgoCD can easily be created through the values.yaml files.
```yaml
# This is copied from https://github.com/rht-labs/ubiquitous-journey/blob/7e23c09465716a5df259c7e41338bb03004fd19f/templates/argo-application.yaml
{{- if .Values.applications }}
{{- $release := .Values.release }}
{{- $source := .Values.source }}
{{- $team := .Values.team }}
{{- range $app := .Values.applications }}
{{- if $app.source }}
{{- $source = $app.source }}
{{- end }}
{{- if $app.enabled }}
---
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  finalizers:
  - resources-finalizer.argocd.argoproj.io
{{- if or (eq $release "ci-cd") (eq $release "cluster-ops") (eq $release "pm") }}
  name: {{ .name }}
{{- else }}
  name: {{ $release }}-{{ .name }}
{{- end }}
  labels:
{{- if .values }}
{{- if .values.fullnameOverride }}
{{- if contains .values.fullnameOverride .name }}
    app.kubernetes.io/instance: {{ .values.fullnameOverride }}
{{- end }}
{{- else }}
    app.kubernetes.io/instance: {{ .name }}
{{- end }}
{{- end }}
spec:
  destination:
    namespace: {{ .destination | default (printf "%s-%s" $team $release) }}
    server: {{ .cluster_api | default "https://kubernetes.default.svc" }}
  project: {{ .project | default "default" }}
  source:
{{- if or .helm_values .values }}
    helm:
{{- if .helm_values }}
      valueFiles:
{{- toYaml .helm_values | nindent 8 }}
{{- end }}
{{- if .values }}
      values: |
{{- toYaml .values | nindent 8 }}
{{- end }}
{{- end }}
    {{- if .source_path }}
    path: {{ .source_path | default "." }}
    {{- end }}
    repoURL: {{ $source }}
    targetRevision: {{ .source_ref | default "main" | quote }}
    {{- if not .source_path }}
    chart: {{ .chart_name | default .name }}
    {{- end }}
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - Validate=true
  ignoreDifferences:
    - group: apps.openshift.io
      kind: DeploymentConfig
      jsonPointers:
        - /spec/replicas
        - /spec/template/spec/containers/0/image
        - /spec/triggers/0/imageChangeParams/lastTriggeredImage
        - /spec/triggers/1/imageChangeParams/lastTriggeredImage
        - /spec/template/metadata/annotations/rollme
    - group: apps
      kind: Deployment
      jsonPointers:
        - /spec/replicas
        - /spec/template/spec/containers/0/image
        - /spec/template/spec/containers/1/image
    - group: build.openshift.io
      kind: BuildConfig
      jsonPointers:
        - /spec/triggers
    - group: route.openshift.io
      kind: Route
      jsonPointers:
        - /status/ingress
        - /spec/host
    - group: integreatly.org
      kind: GrafanaDataSource
      jsonPointers:
        - /spec/datasources/0/secureJsonData/httpHeaderValue1
{{- end }}
{{- end }}
{{- end }}
```

## 7. Launch ArgoCD app of apps
In the root directory of your app of apps repository, run the following commands to launch the initial app of apps that will handle your tooling setup. This assumes you have already logged in using oc.
```
helm upgrade --install banana-gitops  --namespace banana-ci-cd .
```

## 8. Set up webhook from service gitops repository to new service-specific argoCD instance
In your gitops repository, configure a webhook to go to the following URL. This will make it so that ArgoCD syncs towards the repository each time a commit happens to the main/master branch. Make sure to set the content type as `application/json`.
`https://argocd-server-banana-ci-cd.apps.<cluster-domain>/api/webhook`

## 9. Adding an application deployment
Now that you have set up your CI/CD tooling you may want to start actually deploying your applications. To do this, create a new directory structure in your gitops repository and place a values.yaml file in there, like below. This sets up the deployment towards the test namespace. The namespace represents that specific stage/environment. 
```
banana/test/values.yaml
```
The values.yaml should contain an ArgoCD application definition that points to the helm chart of your application, wherever that might be stored, and specifies the values for this deployment. Here you will add all the applications that are part of your service. You can then replicate this for each stage/environment you want to deploy to, with a new directory for each.
```yaml
release: "test" # Must match the environment/directory name
source: "http://nexus.labs-ci-cd.svc.cluster.local:8081/repository/helm-charts/"
##############
# Argo Apps declaration
#############
applications:

  my-banana-app:
    name: my-banana-app
    enabled: true
    source: http://nexus.labs-ci-cd.svc.cluster.local.labs-ci-cd:8081/repository/helm-charts
    chart_name: my-banana-app-chart
    source_ref: 0.0.1 # helm chart version
    values:
      image:
        version: 0.0.1 # container image version
```
Now we will set up a new app of apps to keep track of the test namespace deployments. Uncomment the application in the values.yaml of your root directory that points to the values.yaml you have just created. When you want to deploy to preprod or prod, uncomment the corresponding elements.
```yaml
source: https://github.com/<github-org>/residency-banana-gitops.git
team: banana
release: ci-cd

applications:
# Will manage the CI/CD tooling
  - name: tooling-app-of-banana
    enabled: true
    source_path: "."
    helm_values:
      - tooling/values.yaml

# Uncomment this now
  - name: test-app-of-banana
    enabled: true
    source_path: "."
    helm_values:
      - banana/test/values.yaml

## Uncomment this later when you want to deploy to preprod
#  - name: preprod-app-of-banana
#    enabled: true
#    source_path: "."
#    helm_values:
#      - banana/preprod/values.yaml

## Uncomment this later when you want to deploy to prod
#  - name: prod-app-of-banana
#    enabled: true
#    source_path: "."
#    helm_values:
#      - banana/prod/values.yaml
```
Once again, we launch this manually with Helm to inform ArgoCD that there is a new app of apps that it should keep track of.
```
helm upgrade --install banana-gitops  --namespace banana-ci-cd .
```

ArgoCD will now deploy and keep track of your application whenever it changes. The tekton pipeline will update your gitops repository whenever it builds a new version of your application.

## Examples
https://github.com/cariad-cloud/residency-randp-gitops