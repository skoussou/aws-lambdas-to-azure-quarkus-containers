# Guidelines for getting traffic into a Service Mesh based `Service`

This explains the Openshift Service Mesh Custom Resources required in order to allow and direct traffic into a Service Mesh hosted application from outside the mesh (external to Openshift and/or namespaces in Openshift not part of the Service Mesh)

## Required Customer Resources 

* [`Gateway` (networking.istio.io)](https://docs.openshift.com/container-platform/4.9/service_mesh/v2x/ossm-traffic-manage.html#ossm-routing-gw_routing-traffic)
> You can use a gateway to manage inbound and outbound traffic for your mesh to specify which traffic you want to enter or leave the mesh. Gateway configurations are applied to standalone Envoy proxies that are running at the edge of the mesh, rather than sidecar Envoy proxies running alongside your service workloads.
* [`VirtualService` (networking.istio.io)](https://docs.openshift.com/container-platform/4.9/service_mesh/v2x/ossm-traffic-manage.html#ossm-routing-vs_routing-traffic)
> You can route requests dynamically to multiple versions of a microservice through Red Hat OpenShift Service Mesh with a virtual service. With virtual services, you can:
> - Address multiple application services through a single virtual service. If your mesh uses Kubernetes, for example, you can configure a virtual service to handle all services in a specific namespace. A virtual service enables you to turn a monolithic application into a service comprised of distinct microservices with a seamless consumer experience.
> - Configure traffic rules in combination with gateways to control ingress and egress traffic.


## Decision Outcome

The following explain the residency interim decisions around `Gateway` and `VirtualService` ownership and configuration

* `Gateway`: configuration will be a task owned by a Service Mesh Operator user and handled via [`residency-helm-charts`(]https://github.com/cariad-cloud/residency-helm-charts) repository. All services will use common `Gateway` definitions (currently only HTTP) unless specialized `Gateway` configurations are discovered later on. `Gateway` Custom Resources will be deployed in the `istio-system-{ENV}` namespaces (eg. `istio-system-dev`, `istio-system-test`, `istio-system-preprod`, `istio-system-prod`)
* `VitualService`: configuration will be a task owned by each development team that needs to expose their service to external clients (external to Openshift and/or namespaces in Openshift not part of the Service Mesh) and will point to a `Gateway` Custom Resource in `istio-system-{ENV}` eg. `istio-system-{ENV}/residency-http-gateway `


## `Gateway` Setup

See an example of [`Gateway`](https://github.com/cariad-cloud/residency-helm-charts/tree/main/servicemesh-prod/templates/gateway-http.yaml)

```YAML
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: residency-http-gateway
  namespace: istio-system-prod
spec:
  selector:
    istio: ingressgateway
  servers:
    - hosts:
        - '*'
      port:
        name: http
        number: 80
        protocol: HTTP
```

### `VirtualService` Setup

See an example of [`VirtualService`](https://github.com/cariad-cloud/residency-randp-service-dp-lambda/blob/main/chart/templates/istio-virtualservice.yaml)

```YAML
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: {{ .Values.name }}-virtualservice
spec:
  gateways:
    - {{ .Values.istio_controlnamespace }}/residency-http-gateway
  hosts:
    - '*'
  http:
    - match:
        - uri:
            exact: /randp/register
        - uri:
            prefix: /randp/register
      route:
        - destination:
            host: residency-randp-service-dp-lambda.{{ .Values.namespace }}.svc.cluster.local
          weight: 100
```          
