# Datadog - Set up unified service tagging
## Introduction
This guide will show you how to set up Unified Service Tagging on your Pod/Workload. These tags will be used to automatically enrich application logs and business metrics. [See the Datadog documentation for more information about Unified Service Tagging.](https://docs.datadoghq.com/getting_started/tagging/unified_service_tagging/?tabs=kubernetes)

## Prerequisites
1. You have a pod-based application deployed in OpenShift

## Configuration
In the resource that sets up your Pod (such as Deployment, CronJob, etc) you simply need to set up labels for your resource and the pods. The relevant labels looks like this:
```yaml
  labels:
   . . .
    tags.datadoghq.com/service: my-service-name
    tags.datadoghq.com/env: my-env
    tags.datadoghq.com/version: my-version
```
* `service` should be a consistent name for your service or application throughout all environments.
* `env` should be unique to each environment.
* `version` should refer to the artifact/container/build version.

If you use a Helm chart to deploy your application you can use value substitution to set these properties, for example:
```yaml
  labels:
    tags.datadoghq.com/service: {{ .Values.name }}
    tags.datadoghq.com/env: {{ .Values.env }}
    tags.datadoghq.com/version: {{ .Values.image.version }}
```

## Examples

Below is a complete deployment example (taken from a Helm chart so contains some value substitution)
```yaml
---
kind: Deployment
apiVersion: apps/v1
metadata:
  name: {{ .Values.name }}
  labels:
    app: {{ .Values.name }}
    tags.datadoghq.com/service: {{ .Values.name }}
    tags.datadoghq.com/env: {{ .Values.env }}
    tags.datadoghq.com/version: {{ .Values.image.version }}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: {{ .Values.name }}
  template:
    metadata:
      name: {{ .Values.name }}
      labels:
        app: {{ .Values.name }}
        tags.datadoghq.com/service: {{ .Values.name }}
        tags.datadoghq.com/env: {{ .Values.env }}
        tags.datadoghq.com/version: {{ .Values.image.version }}
      annotations:
        ad.datadoghq.com/quarkus.logs: |
          [
            {
              "source":"openshift"
            }
          ]
    spec:
      containers:
        - resources:
            limits:
              cpu: '400m'
              memory: 1Gi
            requests:
              cpu: '200m'
              memory: 512Mi
          name: quarkus
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
            - name: prometheus
              containerPort: 9779
              protocol: TCP
            - name: jolokia
              containerPort: 8778
              protocol: TCP
          imagePullPolicy: Always
          image: {{ .Values.image.registry }}/{{ .Values.image.repository }}/{{ .Values.image.name }}:{{ .Values.image.version }}
          volumeMounts:
          - name: {{ .Values.name }}-application-properties
            mountPath: /deployments/config          
          - name: wc-test-kafka-cluster-truststore
            mountPath: /deployments/truststore 
          envFrom:
            - secretRef:
                name: {{ .Values.name }}-connection
            - secretRef:
                name: {{ .Values.config.secrets.m2mactorsecret.k8s.secret }}
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      securityContext: {}
      schedulerName: default-scheduler
      volumes:
        - name: {{ .Values.config.secrets.m2mactorsecret.k8s.secret }}
          secret:
            secretName: {{ .Values.config.secrets.m2mactorsecret.k8s.secret }}
        - name:  {{ .Values.config.cosmos.connection.secret.name }}        
          secret:
            secretName: {{ .Values.config.cosmos.connection.secret.name }}
        - name: {{ .Values.name }}-application-properties
          configMap:
            name: {{ .Values.name }}-application-properties
        - name: wc-test-kafka-cluster-truststore
          secret:
            secretName: wc-test-kafka-cluster-truststore                                    
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 25%
      maxSurge: 25%
  revisionHistoryLimit: 2
  progressDeadlineSeconds: 600

```