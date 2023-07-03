# Datadog - Report custom business metrics
## Introduction
This guide will show how to set up business metrics reporting for your quarkus application, and how to forward those metrics to Datadog.

See also
* https://quarkus.io/blog/micrometer-prometheus-openshift/
* https://docs.datadoghq.com/integrations/openmetrics/
* https://docs.datadoghq.com/containers/kubernetes/prometheus/
* https://www.baeldung.com/micrometer

## Prerequisites
1. You have a pod-based quarkus-based application deployed in OpenShift
2. The Datadog agent has been set up inside the cluster.
3. If your application is inside a Service Mesh, the Datadog agent also needs to be associated with the same Service Mesh. Otherwise the Datadog agent cannot reach your pods.

## Configuration
First add the following dependency in your quarkus pom.xml. This will set up a scraping endpoint in your quarkus application that will be populated with metrics, located att `localhost:8080/q/metrics`
```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
      <!-- Set version in case you are not using a bom <version></version> -->
    </dependency>
```
Next - add reporting of a metric in your application by injecting the MeterRegistry and using it to report a metric. In this example it's a simple counter.
```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class SomeClass {
  @Inject
  MeterRegistry registry;

  public void someMethod() {
      registry.counter("metric_name", Tags.of("tagkey", "tagval")).increment();
  }
}

```
Refer to the Micrometer documentation of what can be reported through the registry object.
* https://www.baeldung.com/micrometer

Finally - we will add annotations for autodiscovery so that Datadog knows that we want to report a metric. This is done in the resource that sets up your Pod (such as `Deployment`, `CronJob`, etc). There are two ways of doing this depending on if you are in a Service Mesh or not.

In the non-service mesh method you simply tell Datadog to scrape the `/q/metrics` endpoint for the metrics you need.
```yaml
      annotations:
        ad.datadoghq.com/<REPLACE_WITH_CONTAINER_NAME>.checks: |
          {
            "openmetrics": {
              "instances": [
                  {
                    "openmetrics_endpoint": "http://%%host%%:8080/q/metrics",
                    "namespace": "<metric_prefix>",
                    "metrics": [ "<metric_name>" ]
                  }
              ]
            }
          }

```
In the service mesh method you will tell ServiceMesh to scrape the `/q/metrics` endpoint and add them to the prometheus endpoint, which you then instruct Datadog to scrape instead of /q/metrics. Note that this approach is optional - but will allow you to access more metrics from the same scraping endpoint as well.
```yaml
      annotations:
        prometheus.io/path: /q/metrics
        prometheus.io/port: '8080'
        prometheus.io/scrape: 'true'        
        ad.datadoghq.com/quarkus.checks: |
            {
              "openmetrics": {
                "instances": [
                    {
                      "openmetrics_endpoint": "http://%%host%%:15020/stats/prometheus",
                      "namespace": "<metric_prefix",
                      "metrics": [ "<metric_name>" ]
                    }
                ]
              }
            }


```
* `<REPLACE_WITH_CONTAINER_NAME>` should be replaced with the name (in the yaml definition) of your container
* `<metric_prefix>` will be appended in front of all your collected metrics in Datadog. So the name of your metric in Datadog will be `<metric_prefix>.<metric_name>`
* `<metric_name>` Must correspond to a metric name you are reporting in your application. Note that it should correspond to the name that you set in your code - not the name shown when scraping `localhost:8080/q/metrics`. You can use regex wildcards here - for example you can use `jvm.*` to collect all jvm metrics.
> **NOTE**: Starting in Datadog Agent v7.32.0, in adherence to the OpenMetrics specification standard, counter names ending in `_total` must be specified without the `_tota`l suffix. For example, to collect `promhttp_metric_handler_requests_tota`l, specify the metric name `promhttp_metric_handler_requests`. This submits to Datadog the metric name appended with `.count`, `promhttp_metric_handler_requests.count`. 
>
> Source: https://docs.datadoghq.com/integrations/openmetrics/

Refer to the Datadog documentation for more information
* https://docs.datadoghq.com/containers/kubernetes/prometheus/

When these changes have been deployed you should be able to locate the reported metrics in Datadog.

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
        ad.datadoghq.com/quarkus.checks: |
          {
            "openmetrics": {
              "instances": [
                  {
                    "openmetrics_endpoint": "http://%%host%%:8080/q/metrics",
                    "namespace": "sim.data",
                    "metrics": [ "poll_triggered" ]
                  }
              ]
            }
          }
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