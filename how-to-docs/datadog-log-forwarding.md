# Datadog - Forward application logs
## Introduction
This guide will show you how to set up log forwarding from your pod-based application to Datadog.

## Prerequisites
1. You have a pod-based application deployed in OpenShift
2. The application produces logs with a Datadog compatible format. Refer to the configuration on the Datadog side. Generally any JSON logging library or other standardized logging framework should work.
3. The logs are produced to `System.out` and not to a file.
4. The Datadog agent has been set up inside the cluster and the log forwarding feature has been enabled.
5. If your application is inside a Service Mesh, the Datadog agent also needs to be associated with the same Service Mesh. Otherwise the Datadog agent cannot reach your pods.
6. The logs your application produces are compatible with a log index on the Datadog side

## Configuration
In the resource that sets up your Pod (such as `Deployment`, `CronJob`, etc) you need to set up autodiscovery annotations for your containers. The relevant annotation looks like this:
```yaml
ad.datadoghq.com/<REPLACE_WITH_CONTAINER_NAME>.logs: |
    [
        {
            "<tag_key1>":"<tag_value1>",
            "<tag_key2>":"<tag_value2>",
            "<tag_key3>":"<tag_value3>"
        }
    ]
```
You will need to add one line for each
* `<REPLACE_WITH_CONTAINER_NAME>` should be replaced with the name (in the yaml definition) of your container
* The `"<tag_key>":"<tag_value>"` configure additional tags that will be added to your logs by the datadog agent.

Note that your logs will be automatically tagged with the [Unified Service Tags if you have set them up.](datadog-tagging.md)

If you do not want to set any special tags, the annotation can just look like this:
```yaml
ad.datadoghq.com/<REPLACE_WITH_CONTAINER_NAME>.logs: [{}]
```

For the complete specification - [refer to the Datadog documentation.](https://docs.datadoghq.com/containers/kubernetes/log/?tabs=daemonset#autodiscovery)

When you have deployed your application with the above autodiscovery annotation the logs from annotated containers should end up in Datadog. 

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
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      securityContext: {}
      schedulerName: default-scheduler
      volumes:
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