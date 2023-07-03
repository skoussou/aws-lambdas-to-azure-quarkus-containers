# Observability - Gathering, visualizing and reacting to monitoring data

## Context and Problem Statement

Applications need to emit monitoring data in the form of logs, metrics and traces for inter/intra service calls gathered, analyzed and visualized for functional and non-functional validation for development and higher environment operations.
Organizations need to gather, monitor, alert on metrics from the application landscape along with logs and possibly traces.


## Considered Options

* Openshift Service Mesh in development gathering metrics + traces and visualizing them via KIALI, Prometheus, Grafana, Jaeger (all part of Service Mesh control plane)
* Datadog agent scraping Service Mesh `istio-proxy` prometheus metrics `/stats/prometheus` for application metrics, plus collecting openshift/kubernetes environment metrics. Furthermore, the Datadog agent could also collect traces and logs from the pods.
* Using `DogStatsd` to send metrics to Datadog agent through the Node host, directly from the Pods/application. [See overview here.](https://docs.datadoghq.com/developers/dogstatsd/?tabs=hostagent)
* Using `fluentd` to forward logs to the Datadog API.

## Decision Outcome

We have decided to focus on the Service Mesh/ISTIO dependent approach with the following setup:
* `dev` namespace: will be part of Openshift Service Mesh (OSSM) `istio-system-dev` and will have metrics and traces collected from OSSM Prometheus
* `test`, `preprod` namespaces: will be part of Openshift Service Mesh (OSSM) `istio-system-preprod` and will have metrics only scraped by Datadog

Pros:
* Auto-discovery and collection of exposed custom metrics from applications within Service Mesh.
* Logs, Metrics and Traces can be collected this way.

Cons:
* If an application cannot be part of the Service Mesh it will need an alternative approach to expose metrics and tracing
* Currently no "opt-in" approach for scraping. The Datadog agent gathers everything that is not explicitly excluded. This could have cost implications on the Datadog side.

## How To
 `dev` namespace: Collect metrics from the following 2 paths
  * `pod-ip:15090/stats/prometheus`: exposed by `istio-proxy`
  * `pod-port/metrics`: exposed by the application container

`test`, `preprod` namespace:   
* Applications will have to contain the following annotations (in the order given) to expose both `istio-proxy` and application metrics together
```yaml
spec:
  replicas: 1
  selector:
    matchLabels:
      app: <hello-cosmos>
  template:
    metadata:
      name: <hello-cosmos>
      labels:
        app: <hello-cosmos>
      annotations:
        prometheus.io/path: /q/metrics
        prometheus.io/port: '8080'
        prometheus.io/scrape: 'true'
        sidecar.istio.io/inject: 'true
```        
* Datadog will scrape the metrics from `http://%%host%%:15020/stats/prometheus`

